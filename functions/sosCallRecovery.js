
"use strict";

const twilio = require("twilio");

const { onSchedule } =
  require("firebase-functions/v2/scheduler");

const { defineSecret } =
  require("firebase-functions/params");

const { getDatabase } =
  require("firebase-admin/database");

const logger =
  require("firebase-functions/logger");

const { stopTrackedCall } =
  require("./sosVoice");

const TWILIO_ACCOUNT_SID =
  defineSecret("TWILIO_ACCOUNT_SID");

const TWILIO_AUTH_TOKEN =
  defineSecret("TWILIO_AUTH_TOKEN");

const STALE_ATTEMPT_MS = 120 * 1000;

const TERMINAL_STATUSES = [
  "completed",
  "canceled",
  "failed",
  "busy",
  "no-answer",
  "stopped",
  "skipped"
];

async function recoverAttempt({
  db,
  client,
  attemptRef,
  attempt,
  senderUid,
  eventId
}) {

  if (
    !attempt ||
    TERMINAL_STATUSES.includes(attempt.status) ||
    attempt.accountSid !==
      TWILIO_ACCOUNT_SID.value()
  ) {
    return;
  }

  const [
    eventSnapshot,
    ackSnapshot
  ] = await Promise.all([
    db.ref(
      `sosEvents/${senderUid}/${eventId}`
    ).get(),

    db.ref(
      `sosAcknowledgements/${senderUid}/${eventId}`
    ).get()
  ]);

  const sos = eventSnapshot.val();

  const age =
    Date.now() - attempt.createdAt;

  const stale =
    Number.isFinite(age) &&
    age >= STALE_ATTEMPT_MS;

  const shouldStop =
    sos?.status !== "active" ||
    ackSnapshot.exists() ||
    attempt.cancellationRequested === true ||
    attempt.status === "stop_pending" ||
    stale;

  if (!attempt.callSid) {

    if (stale || shouldStop) {

      await attemptRef.transaction(current => {

        if (
          !current ||
          current.attemptId !== attempt.attemptId ||
          current.callSid ||
          TERMINAL_STATUSES.includes(current.status)
        ) {
          return;
        }

        return {
          ...current,
          status: "creation_unknown",
          cancellationRequested: true,
          recoveryRequired: true,
          updatedAt: Date.now()
        };
      });
    }

    return;
  }

  if (
    !/^CA[a-fA-F0-9]{32}$/.test(attempt.callSid)
  ) {
    return;
  }

  let providerCall;

  try {

    providerCall =
      await client.calls(attempt.callSid).fetch();

  } catch (error) {

    logger.warn(
      "Unable to retrieve SOS provider call status"
    );

    return;
  }

  if (
    TERMINAL_STATUSES.includes(providerCall.status)
  ) {

    await attemptRef.transaction(current => {

      if (
        !current ||
        current.attemptId !== attempt.attemptId ||
        current.callSid !== attempt.callSid ||
        TERMINAL_STATUSES.includes(current.status)
      ) {
        return;
      }

      return {
        ...current,
        status: providerCall.status,
        providerStatus: providerCall.status,
        recoveryRequired: false,
        finishedAt: Date.now(),
        updatedAt: Date.now()
      };
    });

    return;
  }

  if (!shouldStop) {
    return;
  }

  const stopped = await stopTrackedCall({
    client,
    callSid: attempt.callSid
  });

  await attemptRef.transaction(current => {

    if (
      !current ||
      current.attemptId !== attempt.attemptId ||
      current.callSid !== attempt.callSid ||
      TERMINAL_STATUSES.includes(current.status)
    ) {
      return;
    }

    return {
      ...current,
      cancellationRequested: true,
      recoveryRequired: !stopped,
      status: stopped
        ? "stopped"
        : "stop_pending",
      updatedAt: Date.now()
    };
  });
}

exports.recoverSosCalls = onSchedule(
  {
    schedule: "every 1 minutes",
    region: "us-central1",
    timeoutSeconds: 120,
    maxInstances: 1,

    secrets: [
      TWILIO_ACCOUNT_SID,
      TWILIO_AUTH_TOKEN
    ]
  },

  async () => {

    const stagingProject =
      process.env.SOS_STAGING_PROJECT_ID;

    if (
      !stagingProject ||
      process.env.GCLOUD_PROJECT !== stagingProject ||
      stagingProject === "demo-glimpse" ||
      stagingProject === "glimpse-e0aab" ||
      process.env.FUNCTIONS_EMULATOR === "true"
    ) {
      return;
    }

    const db = getDatabase();

    const client = twilio(
      TWILIO_ACCOUNT_SID.value(),
      TWILIO_AUTH_TOKEN.value()
    );

    const attemptsRoot =
      db.ref("sosCallAttempts");

    const snapshot =
      await attemptsRoot.get();

    const senders =
      snapshot.val() || {};

    for (
      const [senderUid, events]
      of Object.entries(senders)
    ) {

      for (
        const [eventId, rounds]
        of Object.entries(events || {})
      ) {

        for (
          const [round, recipients]
          of Object.entries(rounds || {})
        ) {

          for (
            const [recipientUid, attempt]
            of Object.entries(recipients || {})
          ) {

            const attemptRef = attemptsRoot
              .child(senderUid)
              .child(eventId)
              .child(round)
              .child(recipientUid);

            try {

              await recoverAttempt({
                db,
                client,
                attemptRef,
                attempt,
                senderUid,
                eventId
              });

            } catch (error) {

              logger.warn(
                "SOS call recovery attempt failed"
              );
            }
          }
        }
      }
    }
  }
);
