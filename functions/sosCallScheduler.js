
"use strict";

const { getDatabase } =
  require("firebase-admin/database");

const { getFunctions } =
  require("firebase-admin/functions");

const logger =
  require("firebase-functions/logger");

const { getVerifiedEmergencyPhone } =
  require("./sosPhoneSettings");

const MAX_CALL_ROUNDS = 5;
const CALL_RETRY_SECONDS = 90;
const MAX_RECIPIENTS = 5;
const MAX_SOS_AGE_MS = 15 * 60 * 1000;
const ROUND_RECOVERY_TIMEOUT_MS = 180 * 1000;

function validKey(value) {
  return (
    typeof value === "string" &&
    value.length > 0 &&
    value.length <= 128 &&
    !/[.#$\[\]/\u0000-\u001f\u007f]/.test(value)
  );
}

async function getEligibleCallRecipients(
  senderUid,
  eventId
) {
  if (
    !validKey(senderUid) ||
    !validKey(eventId)
  ) {
    return [];
  }

  const db = getDatabase();

  const [
    eventSnapshot,
    ackSnapshot,
    contactsSnapshot
  ] = await Promise.all([
    db.ref(
      `sosEvents/${senderUid}/${eventId}`
    ).get(),

    db.ref(
      `sosAcknowledgements/${senderUid}/${eventId}`
    ).get(),

    db.ref(
      `emergencyContacts/${senderUid}`
    ).get()
  ]);

  const sos = eventSnapshot.val();
  const contacts = contactsSnapshot.val() || {};

  if (
    !sos ||
    sos.status !== "active" ||
    sos.userUid !== senderUid ||
    sos.id !== eventId ||
    ackSnapshot.exists()
  ) {
    return [];
  }

  const age = Date.now() - sos.createdAt;

  if (
    !Number.isFinite(age) ||
    age < 0 ||
    age > MAX_SOS_AGE_MS
  ) {
    return [];
  }

  const requested = sos.recipientUids || {};

  const recipientUids = Object.keys(requested)
    .filter(uid =>
      validKey(uid) &&
      uid !== senderUid &&
      requested[uid] === true &&
      contacts[uid]?.status === "accepted"
    );

  const recipients = [];

  const usedPhoneNumbers = new Set();

  for (const recipientUid of recipientUids) {

    if (recipients.length >= MAX_RECIPIENTS) {
      break;
    }

    try {
      const phoneNumber =
        await getVerifiedEmergencyPhone(recipientUid);

      if (
        phoneNumber &&
        !usedPhoneNumbers.has(phoneNumber)
      ) {
        usedPhoneNumbers.add(phoneNumber);

        recipients.push({
          recipientUid,
          phoneNumber
        });
      }

    } catch (error) {

      logger.warn(
        "Unable to retrieve emergency recipient settings"
      );
    }
  }

  return recipients;
}

async function runCallRound({
  senderUid,
  eventId,
  round,
  placeCall
}) {
  if (
    !validKey(senderUid) ||
    !validKey(eventId) ||
    !Number.isInteger(round) ||
    round < 1 ||
    round > MAX_CALL_ROUNDS ||
    typeof placeCall !== "function"
  ) {
    return;
  }

  const recipients =
    await getEligibleCallRecipients(
      senderUid,
      eventId
    );

  if (recipients.length === 0) {

    logger.info(
      "No eligible SOS call recipients remain"
    );

    return;
  }

  const db = getDatabase();

  const roundRef = db.ref(
    `sosCallRounds/${senderUid}/${eventId}/${round}`
  );

  const claim = await roundRef.transaction(
    current => {
      if (current !== null) return;

      return {
        status: "processing",
        startedAt: Date.now()
      };
    },
    undefined,
    false
  );

  if (!claim.committed) {
    return;
  }
  if (!claim.committed) {

    const existing = claim.snapshot.val();

    if (!existing) {
      return;
    }

    const age = Date.now() - existing.startedAt;

    const interrupted =
      existing.status === "processing" &&
      Number.isFinite(age) &&
      age >= ROUND_RECOVERY_TIMEOUT_MS;

    const schedulingFailed =
      existing.status === "finished" &&
      existing.nextRoundStatus === "scheduling_failed";

    if (interrupted || schedulingFailed) {

      await roundRef.transaction(current => {

        if (
          !current ||
          current.startedAt !== existing.startedAt ||
          current.status !== existing.status ||
          current.nextRoundStatus !== existing.nextRoundStatus
        ) {
          return;
        }

        return {
          ...current,
          recoveryRequired: true,
          recoveryDetectedAt: Date.now()
        };
      });
    }

    return;
  }

  let attempted = 0;
  let failed = 0;
  let skipped = 0;

  for (const recipient of recipients) {

    const eligible =
      await getEligibleCallRecipients(
        senderUid,
        eventId
      );

    const currentRecipient = eligible.find(
      item =>
        item.recipientUid === recipient.recipientUid
    );

    if (!currentRecipient) {
      skipped++;
      continue;
    }

    try {
      const result = await placeCall({
        senderUid,
        eventId,
        recipientUid:
          currentRecipient.recipientUid,

        phoneNumber:
          currentRecipient.phoneNumber,

        round
      });

      if (result.skipped) {
        skipped++;
      } else {
        attempted++;
      }

    } catch (error) {

      failed++;

      logger.warn(
        "SOS call attempt failed"
      );
    }
  }

  await roundRef.update({
    status: "finished",
    attempted,
    failed,
    skipped,
    finishedAt: Date.now()
  });

  const remaining =
    await getEligibleCallRecipients(
      senderUid,
      eventId
    );

  if (remaining.length === 0) {

    await roundRef.update({
      nextRoundStatus: "not_required"
    });

    return;
  }

  if (round >= MAX_CALL_ROUNDS) {

    await roundRef.update({
      nextRoundStatus: "retry_limit_reached"
    });

    logger.warn(
      "SOS telephone retry limit reached"
    );

    return;
  }

  await roundRef.update({
    nextRoundStatus: "scheduling"
  });

  try {

    await getFunctions()
      .taskQueue("advanceSos")
      .enqueue(
        {
          senderUid,
          eventId,
          stage: 3,
          round: round + 1
        },
        {
          scheduleDelaySeconds:
            CALL_RETRY_SECONDS
        }
      );

    await roundRef.update({
      nextRoundStatus: "scheduled",
      nextRound: round + 1,
      nextRoundScheduledAt: Date.now()
    });

  } catch (error) {

    await roundRef.update({
      nextRoundStatus: "scheduling_failed"
    });

    throw error;
  }
}

module.exports = {
  runCallRound,
  getEligibleCallRecipients
};
