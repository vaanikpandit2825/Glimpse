
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
  if (!validKey(senderUid) || !validKey(eventId)) {
    return [];
  }

  const db = getDatabase();

  const [
    eventSnapshot,
    ackSnapshot,
    contactsSnapshot
  ] = await Promise.all([
    db.ref(`sosEvents/${senderUid}/${eventId}`).get(),

    db.ref(
      `sosAcknowledgements/${senderUid}/${eventId}`
    ).get(),

    db.ref(`emergencyContacts/${senderUid}`).get()
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
    )
    .slice(0, MAX_RECIPIENTS);

  const recipients = [];

  for (const recipientUid of recipientUids) {

    try {
      const phoneNumber =
        await getVerifiedEmergencyPhone(recipientUid);

      if (phoneNumber) {
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
    !Number.isInteger(round) ||
    round < 1 ||
    round > MAX_CALL_ROUNDS
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

  // Prevent duplicate processing of the same round.
  const claim = await roundRef.transaction(current => {

    if (current !== null) {
      return;
    }

    return {
      status: "processing",
      startedAt: Date.now()
    };

  }, undefined, false);

  if (!claim.committed) {
    return;
  }

  let attempted = 0;

  for (const recipient of recipients) {

    // Recheck consent, acknowledgment and event status.
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
      continue;
    }

    try {

      const result = await placeCall({
        senderUid,
        eventId,
        recipientUid: currentRecipient.recipientUid,
        phoneNumber: currentRecipient.phoneNumber,
        round
      });

      if (!result.skipped) {
        attempted++;
      }

    } catch (error) {

      logger.warn("SOS call attempt failed");
    }
  }

  await roundRef.update({
    status: "finished",
    attempted,
    finishedAt: Date.now()
  });

  const remaining =
    await getEligibleCallRecipients(
      senderUid,
      eventId
    );

  if (
    remaining.length > 0 &&
    round < MAX_CALL_ROUNDS
  ) {

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
          scheduleDelaySeconds: CALL_RETRY_SECONDS
        }
      );
  }
}

module.exports = {
  runCallRound,
  getEligibleCallRecipients
};
