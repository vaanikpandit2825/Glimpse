
"use strict";

const twilio = require("twilio");

const { onRequest } =
  require("firebase-functions/v2/https");

const { defineSecret } =
  require("firebase-functions/params");

const { getDatabase } =
  require("firebase-admin/database");

const { stopTrackedCall } =
  require("./sosVoice");

const TWILIO_AUTH_TOKEN =
  defineSecret("TWILIO_AUTH_TOKEN");

const TWILIO_ACCOUNT_SID =
  defineSecret("TWILIO_ACCOUNT_SID");

const TERMINAL_STATUSES = [
  "completed",
  "busy",
  "failed",
  "no-answer",
  "canceled"
];

const VALID_STATUSES = [
  "queued",
  "initiated",
  "ringing",
  "in-progress",
  ...TERMINAL_STATUSES
];

function validKey(value) {
  return (
    typeof value === "string" &&
    value.length > 0 &&
    value.length <= 128 &&
    !/[.#$\[\]/\u0000-\u001f\u007f]/.test(value)
  );
}

exports.sosCallStatus = onRequest(
  {
    region: "us-central1",
    secrets: [
      TWILIO_AUTH_TOKEN,
      TWILIO_ACCOUNT_SID
    ]
  },

  async (req, res) => {

    const stagingProject =
      process.env.SOS_STAGING_PROJECT_ID;

    if (
      !stagingProject ||
      process.env.GCLOUD_PROJECT !== stagingProject ||
      stagingProject === "demo-glimpse" ||
      stagingProject === "glimpse-e0aab" ||
      process.env.FUNCTIONS_EMULATOR === "true"
    ) {
      return res.status(403)
        .send("Status service disabled");
    }

    if (req.method !== "POST") {
      return res.status(405)
        .send("Method not allowed");
    }

    const callbackUrl =
      process.env.SOS_CALL_STATUS_URL;

    if (!callbackUrl) {
      return res.status(503)
        .send("Status service unavailable");
    }

    const signature =
      req.get("X-Twilio-Signature") || "";

    const queryIndex =
      req.originalUrl.indexOf("?");

    const rawQuery = queryIndex >= 0
      ? req.originalUrl.substring(queryIndex)
      : "";

    const validSignature =
      twilio.validateRequest(
        TWILIO_AUTH_TOKEN.value(),
        signature,
        callbackUrl + rawQuery,
        req.body || {}
      );

    if (!validSignature) {
      return res.status(403)
        .send("Invalid signature");
    }

    const {
      senderUid,
      eventId,
      recipientUid,
      round,
      attemptId
    } = req.query;

    const roundNumber = Number(round);

    const callSid = req.body?.CallSid;

    const accountSid = req.body?.AccountSid;

    const providerStatus =
      req.body?.CallStatus;

    const sequenceNumber =
      Number(req.body?.SequenceNumber);

    if (
      !validKey(senderUid) ||
      !validKey(eventId) ||
      !validKey(recipientUid) ||
      !validKey(attemptId) ||
      typeof round !== "string" ||
      !Number.isInteger(roundNumber) ||
      roundNumber < 1 ||
      roundNumber > 5 ||
      String(roundNumber) !== round ||
      typeof callSid !== "string" ||
      !/^CA[a-fA-F0-9]{32}$/.test(callSid) ||
      accountSid !== TWILIO_ACCOUNT_SID.value() ||
      !VALID_STATUSES.includes(providerStatus) ||
      req.body?.SequenceNumber === undefined ||
      !Number.isSafeInteger(sequenceNumber) ||
      sequenceNumber < 0
    ) {
      return res.status(400)
        .send("Invalid callback details");
    }

    const db = getDatabase();

    const attemptRef = db.ref(
      `sosCallAttempts/${senderUid}/${eventId}/${roundNumber}/${recipientUid}`
    );

    const result = await attemptRef.transaction(
      current => {

        if (
          !current ||
          current.attemptId !== attemptId ||
          current.accountSid !== accountSid ||
          current.senderUid !== senderUid ||
          current.eventId !== eventId ||
          current.recipientUid !== recipientUid ||
          current.round !== roundNumber ||
          (
            current.callSid &&
            current.callSid !== callSid
          )
        ) {
          return;
        }

        if (
          Number.isInteger(current.lastSequenceNumber) &&
          sequenceNumber <= current.lastSequenceNumber
        ) {
          return;
        }

        if (
          TERMINAL_STATUSES.includes(current.status) ||
          current.status === "stopped"
        ) {
          return;
        }

        const updated = {
          ...current,
          callSid,
          providerStatus,
          lastSequenceNumber: sequenceNumber,
          updatedAt: Date.now()
        };

        if (providerStatus === "in-progress") {
          updated.answeredAt =
            current.answeredAt || Date.now();
        }

        if (
          TERMINAL_STATUSES.includes(providerStatus)
        ) {
          updated.status = providerStatus;
          updated.finishedAt = Date.now();
        }

        return updated;
      },
      undefined,
      false
    );

    const attempt = result.snapshot.val();

    if (
      !attempt ||
      attempt.attemptId !== attemptId ||
      attempt.callSid !== callSid ||
      attempt.accountSid !== accountSid
    ) {
      return res.status(204).send();
    }

    if (
      TERMINAL_STATUSES.includes(attempt.status) ||
      attempt.status === "stopped"
    ) {
      return res.status(204).send();
    }

    const [eventSnapshot, ackSnapshot] =
      await Promise.all([
        db.ref(
          `sosEvents/${senderUid}/${eventId}`
        ).get(),

        db.ref(
          `sosAcknowledgements/${senderUid}/${eventId}`
        ).get()
      ]);

    const sos = eventSnapshot.val();

    const isAcknowledgingCall =
      ackSnapshot.val()?.callSid === callSid &&
      ackSnapshot.val()?.method === "voice_call";

    const shouldStop =
      attempt.cancellationRequested === true ||
      sos?.status !== "active" ||
      (
        ackSnapshot.exists() &&
        !isAcknowledgingCall
      );

    if (shouldStop) {

      const client = twilio(
        TWILIO_ACCOUNT_SID.value(),
        TWILIO_AUTH_TOKEN.value()
      );

      const stopped = await stopTrackedCall({
        client,
        callSid
      });

      await attemptRef.transaction(current => {

        if (
          !current ||
          current.attemptId !== attemptId ||
          current.callSid !== callSid ||
          TERMINAL_STATUSES.includes(current.status) ||
          current.status === "stopped"
        ) {
          return;
        }

        return {
          ...current,
          cancellationRequested: true,
          status: stopped
            ? "stopped"
            : "stop_pending",
          updatedAt: Date.now()
        };
      });

      if (!stopped) {
        return res.status(503)
          .send("Cancellation pending");
      }
    }

    return res.status(204).send();
  }
);
