
"use strict";

const twilio = require("twilio");

const { getDatabase } =
  require("firebase-admin/database");

const logger =
  require("firebase-functions/logger");

const { stopTrackedCall } =
  require("./sosVoice");

function validKey(value) {
  return (
    typeof value === "string" &&
    value.length > 0 &&
    value.length <= 128 &&
    !/[.#$\[\]/\u0000-\u001f\u007f]/.test(value)
  );
}

async function cancelOutstandingCalls({
  senderUid,
  eventId,
  accountSid,
  authToken,
  exceptCallSid = null
}) {

  const stagingProject =
    process.env.SOS_STAGING_PROJECT_ID;

  if (
    !stagingProject ||
    process.env.GCLOUD_PROJECT !== stagingProject ||
    stagingProject === "demo-glimpse" ||
    stagingProject === "glimpse-e0aab" ||
    process.env.FUNCTIONS_EMULATOR === "true"
  ) {
    return {
      stopped: 0,
      pending: 0
    };
  }

  if (
    !validKey(senderUid) ||
    !validKey(eventId)
  ) {
    throw new Error("Invalid SOS details");
  }

  const db = getDatabase();

  const eventRef = db.ref(
    `sosEvents/${senderUid}/${eventId}`
  );

  const ackRef = db.ref(
    `sosAcknowledgements/${senderUid}/${eventId}`
  );

  const [eventSnapshot, ackSnapshot] =
    await Promise.all([
      eventRef.get(),
      ackRef.get()
    ]);

  const event = eventSnapshot.val();

  if (
    event?.status === "active" &&
    !ackSnapshot.exists()
  ) {
    return {
      stopped: 0,
      pending: 0
    };
  }

  const attemptsRef = db.ref(
    `sosCallAttempts/${senderUid}/${eventId}`
  );

  const snapshot = await attemptsRef.get();

  const rounds = snapshot.val() || {};

  const client = twilio(
    accountSid,
    authToken
  );

  let stopped = 0;
  let pending = 0;

  const terminalStatuses = [
    "stopped",
    "skipped",
    "completed",
    "canceled",
    "failed",
    "busy",
    "no-answer"
  ];

  for (
    const [round, recipients]
    of Object.entries(rounds)
  ) {

    for (
      const [recipientUid, attempt]
      of Object.entries(recipients || {})
    ) {

      if (
        !attempt ||
        attempt.accountSid !== accountSid ||
        terminalStatuses.includes(attempt.status)
      ) {
        continue;
      }

      if (
        exceptCallSid &&
        attempt.callSid === exceptCallSid
      ) {
        continue;
      }

      const attemptRef = attemptsRef
        .child(round)
        .child(recipientUid);

      if (!attempt.callSid) {

        await attemptRef.transaction(current => {
          if (
            !current ||
            current.attemptId !== attempt.attemptId ||
            terminalStatuses.includes(current.status)
          ) {
            return;
          }

          return {
            ...current,
            cancellationRequested: true,
            updatedAt: Date.now()
          };
        });

        pending++;
        continue;
      }

      const cancelled = await stopTrackedCall({
        client,
        callSid: attempt.callSid
      });

      await attemptRef.transaction(current => {

        if (
          !current ||
          current.attemptId !== attempt.attemptId ||
          terminalStatuses.includes(current.status)
        ) {
          return;
        }

        return {
          ...current,
          cancellationRequested: true,
          status: cancelled
            ? "stopped"
            : "stop_pending",
          updatedAt: Date.now()
        };
      });

      if (cancelled) {
        stopped++;
      } else {
        pending++;
      }
    }
  }

  logger.info("SOS call cancellation processed", {
    stopped,
    pending
  });

  return {
    stopped,
    pending
  };
}

module.exports = {
  cancelOutstandingCalls
};
