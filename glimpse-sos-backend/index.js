
const express = require("express");

const {
  initializeApp,
  applicationDefault,
  cert,
  getApps,
} = require("firebase-admin/app");

const { getAuth } = require("firebase-admin/auth");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");

const app = express();

app.use(express.json({ limit: "20kb" }));

const DATABASE_URL =
  "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app";

const CLAIM_TTL_MS = 2 * 60 * 1000;

if (getApps().length === 0) {
  const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;

  initializeApp({
    credential: serviceAccountJson
      ? cert(JSON.parse(serviceAccountJson))
      : applicationDefault(),
    databaseURL: DATABASE_URL,
  });
}

app.get("/", (req, res) => {
  res.status(200).json({
    service: "GLIMPSE SOS Backend",
    status: "running",
  });
});

app.get("/health/firebase", async (req, res) => {
  try {
    await getDatabase()
      .ref("sosEvents")
      .limitToFirst(1)
      .once("value");

    res.status(200).json({
      firebase: "connected",
      sosEventsRead: "success",
    });
  } catch (error) {
    console.error("Firebase health check failed:", error.message);

    res.status(500).json({
      firebase: "error",
      message: "Could not read SOS data",
    });
  }
});

app.post("/sos/notify", async (req, res) => {
  let dispatchRef = null;
  let claimed = false;

  try {
    // 1. Verify the caller's Firebase ID token.
    const authorization = req.get("Authorization") || "";
    const match = authorization.match(/^Bearer (.+)$/i);

    if (!match) {
      return res.status(401).json({
        error: "Missing Firebase ID token",
      });
    }

    const decodedToken = await getAuth().verifyIdToken(match[1]);
    const senderUid = decodedToken.uid;

    const eventId = req.body?.eventId;

    if (
      typeof eventId !== "string" ||
      eventId.length === 0 ||
      eventId.length > 200 ||
      eventId.includes("/")
    ) {
      return res.status(400).json({
        error: "A valid eventId is required",
      });
    }

    const database = getDatabase();

    // 2. Load the SOS event belonging to the authenticated sender.
    const eventRef = database
      .ref("sosEvents")
      .child(senderUid)
      .child(eventId);

    const eventSnapshot = await eventRef.once("value");
    const event = eventSnapshot.val();

    if (!event) {
      return res.status(404).json({
        error: "SOS event not found",
      });
    }

    if (event.userUid !== senderUid) {
      return res.status(403).json({
        error: "SOS event owner does not match",
      });
    }

    if (event.status !== "active") {
      return res.status(409).json({
        error: "SOS event is not active",
      });
    }

    // 3. Get the recipients listed on the SOS event.
    const recipientMap = event.recipientUids || {};

    const requestedRecipients = Object.keys(recipientMap).filter(
      (uid) => uid !== senderUid && recipientMap[uid] === true
    );

    if (requestedRecipients.length === 0) {
      return res.status(200).json({
        message: "No connected recipients were listed",
        notificationsSent: 0,
      });
    }

    // 4. Independently confirm that each recipient is still connected.
    const connectionChecks = await Promise.all(
      requestedRecipients.map(async (uid) => {
        const snapshot = await database
          .ref("connections")
          .child(senderUid)
          .child(uid)
          .child("status")
          .once("value");

        return snapshot.val() === "connected" ? uid : null;
      })
    );

    const recipientUids = connectionChecks.filter(Boolean);

    if (recipientUids.length === 0) {
      return res.status(200).json({
        message: "No eligible connected recipients",
        notificationsSent: 0,
      });
    }

    // 5. Collect registered FCM tokens and remember their owners.
    const tokenOwners = new Map();

    const tokenSnapshots = await Promise.all(
      recipientUids.map(async (uid) => {
        const snapshot = await database
          .ref("fcmTokens")
          .child(uid)
          .once("value");

        return { uid, tokens: snapshot.val() || {} };
      })
    );

    for (const { uid, tokens } of tokenSnapshots) {
      for (const [token, enabled] of Object.entries(tokens)) {
        if (enabled === true) {
          // Avoid sending the same token more than once.
          if (!tokenOwners.has(token)) {
            tokenOwners.set(token, uid);
          }
        }
      }
    }

    const tokenEntries = Array.from(tokenOwners.entries()).map(
      ([token, uid]) => ({ token, uid })
    );

    if (tokenEntries.length === 0) {
      return res.status(200).json({
        message: "No registered recipient devices found",
        notificationsSent: 0,
      });
    }

    // 6. Atomically claim this event so concurrent requests cannot
    // send the same alert twice.
    dispatchRef = database
      .ref("sosNotificationDispatch")
      .child(senderUid)
      .child(eventId);

    const now = Date.now();

    const claimResult = await dispatchRef.transaction((current) => {
      if (current?.status === "sent") {
        return;
      }

      if (
        current?.status === "sending" &&
        now - current.startedAt < CLAIM_TTL_MS
      ) {
        return;
      }

      return {
        status: "sending",
        startedAt: now,
      };
    });

    if (!claimResult.committed) {
      return res.status(409).json({
        error: "This SOS notification is already being processed or was sent",
      });
    }

    claimed = true;

    // 7. Send notifications in batches of at most 500 tokens.
    let notificationsSent = 0;
    let notificationsFailed = 0;

    const invalidTokenEntries = [];

    for (let i = 0; i < tokenEntries.length; i += 500) {
      const batch = tokenEntries.slice(i, i + 500);

      const result = await getMessaging().sendEachForMulticast({
        tokens: batch.map((entry) => entry.token),

        notification: {
          title: "GLIMPSE SOS ALERT",
          body: "A connected contact needs help. Tap to view their emergency details.",
        },

        data: {
          type: "SOS",
          senderUid,
          eventId,
        },

        android: {
          priority: "high",
          notification: {
            channelId: "glimpse_sos_alerts",
            sound: "default",
          },
        },
      });

      notificationsSent += result.successCount;
      notificationsFailed += result.failureCount;

      result.responses.forEach((response, index) => {
        if (
          !response.success &&
          response.error &&
          [
            "messaging/registration-token-not-registered",
            "messaging/invalid-registration-token",
          ].includes(response.error.code)
        ) {
          invalidTokenEntries.push(batch[index]);
        }
      });
    }

    // 8. Remove invalid tokens from their actual owners' paths.
    await Promise.all(
      invalidTokenEntries.map(({ uid, token }) =>
        database
          .ref("fcmTokens")
          .child(uid)
          .child(token)
          .remove()
          .catch((error) => {
            console.error("Could not remove invalid FCM token:", error.message);
          })
      )
    );

    // 9. Record the outcome to prevent duplicate alerts.
    if (notificationsSent > 0) {
      await dispatchRef.update({
        status: "sent",
        completedAt: Date.now(),
        notificationsSent,
        notificationsFailed,
      });

      claimed = false;

      return res.status(200).json({
        message: "SOS notification dispatch completed",
        notificationsSent,
        notificationsFailed,
        invalidTokensRemoved: invalidTokenEntries.length,
      });
    }

    // Nothing was delivered, so allow a later retry.
    await dispatchRef.remove();
    claimed = false;

    return res.status(502).json({
      error: "No SOS notifications were delivered",
      notificationsSent,
      notificationsFailed,
    });
  } catch (error) {
    console.error("SOS notification error:", error.message);

    if (claimed && dispatchRef) {
      await dispatchRef.remove().catch((cleanupError) => {
        console.error(
          "Could not release notification claim:",
          cleanupError.message
        );
      });
    }

    if (error.code === "auth/id-token-expired" ||
        error.code === "auth/argument-error" ||
        error.code === "auth/invalid-id-token") {
      return res.status(401).json({
        error: "Invalid or expired Firebase ID token",
      });
    }

    return res.status(500).json({
      error: "Could not process the SOS notification",
    });
  }
});

const PORT = process.env.PORT || 3000;

app.listen(PORT, "0.0.0.0", () => {
  console.log(`GLIMPSE SOS backend listening on port ${PORT}`);
});
