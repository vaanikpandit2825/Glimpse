
const DATABASE_URL =
  "https://glimpse-e0aab-default-rtdb.asia-southeast1.firebasedatabase.app";

const PROJECT_ID = "glimpse-e0aab";

const base64url = (value) =>
  btoa(String.fromCharCode(...new Uint8Array(value)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");

const encodeText = (value) =>
  base64url(new TextEncoder().encode(value));

async function getGoogleAccessToken(serviceAccount) {
  const now = Math.floor(Date.now() / 1000);

  const unsignedToken = [
    encodeText(JSON.stringify({ alg: "RS256", typ: "JWT" })),
    encodeText({
      iss: serviceAccount.client_email,
      scope:
        "https://www.googleapis.com/auth/userinfo.email " +
        "https://www.googleapis.com/auth/firebase.database " +
        "https://www.googleapis.com/auth/firebase.messaging",
      aud: serviceAccount.token_uri,
      iat: now,
      exp: now + 3600,
    }),
  ].join(".");

  const pem = serviceAccount.private_key
    .replace(/-----BEGIN PRIVATE KEY-----/g, "")
    .replace(/-----END PRIVATE KEY-----/g, "")
    .replace(/\s/g, "");

  const binary = Uint8Array.from(atob(pem), (c) =>
    c.charCodeAt(0)
  );

  const key = await crypto.subtle.importKey(
    "pkcs8",
    binary,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    key,
    new TextEncoder().encode(unsignedToken)
  );

  const assertion = `${unsignedToken}.${base64url(signature)}`;

  const response = await fetch(serviceAccount.token_uri, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion,
    }),
  });

  if (!response.ok) {
    throw new Error("Google authentication failed");
  }

  const result = await response.json();

  if (!result.access_token) {
    throw new Error("Google access token missing");
  }

  return result.access_token;
}

async function verifyFirebaseIdToken(idToken, apiKey) {
  const response = await fetch(
    `https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=${encodeURIComponent(apiKey)}`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ idToken }),
    }
  );

  if (!response.ok) {
    throw new Error("Invalid or expired Firebase ID token");
  }

  const result = await response.json();
  const user = result.users?.[0];

  if (!user?.localId) {
    throw new Error("Firebase user could not be verified");
  }

  return user.localId;
}

async function getServiceAccount(env) {
  if (!env.FIREBASE_SERVICE_ACCOUNT_JSON) {
    throw new Error("Firebase service account is not configured");
  }

  const account = JSON.parse(env.FIREBASE_SERVICE_ACCOUNT_JSON);

  if (
    account.project_id !== PROJECT_ID ||
    !account.client_email ||
    !account.private_key ||
    !account.token_uri
  ) {
    throw new Error("Invalid service-account configuration");
  }

  return account;
}

async function databaseRequest(path, accessToken, options = {}) {
  const response = await fetch(
    `${DATABASE_URL}/${path}.json`,
    {
      ...options,
      headers: {
        Authorization: `Bearer ${accessToken}`,
        Accept: "application/json",
        ...(options.headers || {}),
      },
    }
  );

  if (!response.ok) {
    await response.body?.cancel();
    throw new Error(`Realtime Database request failed (${response.status})`);
  }

  return response;
}

async function readDatabase(path, accessToken) {
  const response = await databaseRequest(path, accessToken);
  return response.json();
}

async function claimNotification(senderUid, eventId, accessToken) {
  // Claim at a separate path so the SOS event itself is not overwritten.
  // ETag + If-Match makes the initial claim conditional and atomic.
  const path =
    `sosNotificationClaims/${encodeURIComponent(senderUid)}/` +
    `${encodeURIComponent(eventId)}`;

  const current = await databaseRequest(path, accessToken, {
    headers: { "X-Firebase-ETag": "true" },
  });

  const existingClaim = await current.json();
  const etag = current.headers.get("ETag");

  if (existingClaim !== null || !etag) {
    return false;
  }

  const claimResponse = await fetch(`${DATABASE_URL}/${path}.json`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
      "If-Match": etag,
    },
    body: JSON.stringify({
      status: "sending",
      createdAt: Date.now(),
    }),
  });

  if (claimResponse.status === 412) {
    await claimResponse.body?.cancel();
    return false;
  }

  if (!claimResponse.ok) {
    await claimResponse.body?.cancel();
    throw new Error("Could not claim SOS notification");
  }

  await claimResponse.body?.cancel();
  return true;
}

async function updateClaim(senderUid, eventId, accessToken, result) {
  const path =
    `sosNotificationClaims/${encodeURIComponent(senderUid)}/` +
    `${encodeURIComponent(eventId)}`;

  const response = await databaseRequest(path, accessToken, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      ...result,
      updatedAt: Date.now(),
    }),
  });

  await response.body?.cancel();
}

async function sendFcmMessage(token, senderUid, eventId, accessToken) {
  const response = await fetch(
    `https://fcm.googleapis.com/v1/projects/${PROJECT_ID}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          token,
          notification: {
            title: "GLIMPSE SOS alert",
            body: "A connected contact has sent an SOS alert.",
          },
          data: {
            type: "sos",
            senderUid,
            eventId,
          },
          android: {
            priority: "high",
            notification: {
              channel_id: "glimpse_sos_alerts",
              sound: "default",
            },
          },
        },
      }),
    }
  );

  if (!response.ok) {
    // Do not log registration tokens or raw FCM response bodies.
    await response.body?.cancel();
    return false;
  }

  await response.body?.cancel();
  return true;
}

async function notifySos(senderUid, eventId, accessToken) {
  const eventPath =
    `sosEvents/${encodeURIComponent(senderUid)}/` +
    `${encodeURIComponent(eventId)}`;

  const event = await readDatabase(eventPath, accessToken);

  if (!event || event.userUid !== senderUid) {
    return {
      status: 404,
      body: { error: "SOS event not found for this sender" },
    };
  }

  if (event.status !== "active") {
    return {
      status: 409,
      body: { error: "SOS event is not active" },
    };
  }

  const recipients = event.recipientUids || {};

  const connections = await readDatabase(
    `connections/${encodeURIComponent(senderUid)}`,
    accessToken
  );

  const eligibleUids = Object.keys(recipients).filter(
    (uid) =>
      recipients[uid] === true &&
      connections?.[uid]?.status === "connected" &&
      uid !== senderUid
  );

  if (eligibleUids.length === 0) {
    return {
      status: 200,
      body: {
        notificationStatus: "no_eligible_recipients",
        attempted: 0,
        sent: 0,
        failed: 0,
      },
    };
  }

  const claimed = await claimNotification(
    senderUid,
    eventId,
    accessToken
  );

  if (!claimed) {
    return {
      status: 409,
      body: { error: "Notifications already claimed for this SOS event" },
    };
  }

  let attempted = 0;
  let sent = 0;
  let failed = 0;

  try {
    for (const uid of eligibleUids) {
      const tokens = await readDatabase(
        `fcmTokens/${encodeURIComponent(uid)}`,
        accessToken
      );

      // Current Android repository stores each token as a child key
      // with value true. Also accept string values for compatibility.
      const deviceTokens = tokens && typeof tokens === "object"
        ? Object.entries(tokens)
            .filter(([key, value]) =>
              typeof key === "string" &&
              key.length > 20 &&
              (value === true || typeof value === "string")
            )
            .map(([key, value]) =>
              typeof value === "string" ? value : key
            )
        : [];

      for (const token of new Set(deviceTokens)) {
        attempted++;

        try {
          const deliveredToFcm = await sendFcmMessage(
            token,
            senderUid,
            eventId,
            accessToken
          );

          if (deliveredToFcm) {
            sent++;
          } else {
            failed++;
          }
        } catch {
          failed++;
        }
      }
    }

    const notificationStatus =
      failed === 0 && sent > 0
        ? "sent"
        : sent > 0
          ? "partially_sent"
          : "failed";

    await updateClaim(senderUid, eventId, accessToken, {
      status: notificationStatus,
      attempted,
      sent,
      failed,
    });

    return {
      status: 200,
      body: {
        notificationStatus,
        attempted,
        sent,
        failed,
      },
    };
  } catch (error) {
    // Keep the claim to avoid accidental duplicate alerts.
    // A stuck claim requires deliberate recovery before retrying.
    try {
      await updateClaim(senderUid, eventId, accessToken, {
        status: "failed",
        attempted,
        sent,
        failed,
      });
    } catch {
      // Preserve the original failure.
    }

    throw error;
  }
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (url.pathname === "/health" && request.method === "GET") {
      return Response.json({
        status: "ok",
        service: "glimpse-sos-backend",
      });
    }

    if (url.pathname === "/sos/notify") {
      if (request.method !== "POST") {
        return Response.json(
          { error: "Method not allowed" },
          { status: 405 }
        );
      }

      const authorization = request.headers.get("Authorization") || "";
      const match = authorization.match(/^Bearer (.+)$/);

      if (!match || !env.FIREBASE_WEB_API_KEY) {
        return Response.json({ error: "Unauthorized" }, { status: 401 });
      }

      let body;

      try {
        body = await request.json();
      } catch {
        return Response.json({ error: "Invalid JSON body" }, { status: 400 });
      }

      const eventId = body?.eventId;

      if (
        typeof eventId !== "string" ||
        !/^[A-Za-z0-9_-]{1,128}$/.test(eventId)
      ) {
        return Response.json({ error: "Invalid event ID" }, { status: 400 });
      }

      try {
        const senderUid = await verifyFirebaseIdToken(
          match[1],
          env.FIREBASE_WEB_API_KEY
        );

        const serviceAccount = await getServiceAccount(env);
        const accessToken = await getGoogleAccessToken(serviceAccount);

        const result = await notifySos(
          senderUid,
          eventId,
          accessToken
        );

        return Response.json(result.body, { status: result.status });
      } catch (error) {
        console.error(
          "SOS notification request failed:",
          error instanceof Error ? error.message : "Unknown error"
        );

        return Response.json(
          { error: "SOS notification processing failed" },
          { status: 502 }
        );
      }
    }

    // The public Firebase Admin diagnostic endpoint has been removed.
    return Response.json({ error: "Not found" }, { status: 404 });
  },
};
