
"use strict";

const twilio = require("twilio");
const { getDatabase } = require("firebase-admin/database");
const { getVerifiedEmergencyPhone } =
  require("./sosPhoneSettings");

const MAX_CALL_DURATION_SECONDS = 45;

async function startEmergencyCall({
  senderUid,
  eventId,
  recipientUid,
  phoneNumber,
  accountSid,
  authToken,
  fromNumber,
  webhookBaseUrl
}) {
  // Never place real calls from the Firebase Emulator.
  if (
    process.env.FUNCTIONS_EMULATOR === "true" ||
    process.env.GCLOUD_PROJECT === "demo-glimpse"
  ) {
    return { skipped: true, reason: "local_environment" };
  }

  if (process.env.SOS_VOICE_ENABLED !== "true") {
    return { skipped: true, reason: "voice_disabled" };
  }

  const db = getDatabase();

  const [eventSnapshot, ackSnapshot, contactSnapshot] =
    await Promise.all([
      db.ref(`sosEvents/${senderUid}/${eventId}`).get(),
      db.ref(`sosAcknowledgements/${senderUid}/${eventId}`).get(),
      db.ref(`emergencyContacts/${senderUid}/${recipientUid}`).get()
    ]);

  const event = eventSnapshot.val();
  const contact = contactSnapshot.val();

  if (
    !event ||
    event.status !== "active" ||
    event.userUid !== senderUid ||
    event.id !== eventId ||
    ackSnapshot.exists() ||
    event.recipientUids?.[recipientUid] !== true ||
    contact?.status !== "accepted"
  ) {
    return { skipped: true, reason: "not_eligible" };
  }
const verifiedPhone =
  await getVerifiedEmergencyPhone(recipientUid);

if (
  !verifiedPhone ||
  verifiedPhone !== phoneNumber
) {
  return {
    skipped: true,
    reason: "phone_or_consent_invalid"
  };
}
  if (
    typeof phoneNumber !== "string" ||
    !/^\+[1-9]\d{7,14}$/.test(phoneNumber)
  ) {
    throw new Error("Recipient phone number must use E.164 format");
  }

  const client = twilio(accountSid, authToken);

  const url = new URL(webhookBaseUrl);

  url.searchParams.set("senderUid", senderUid);
  url.searchParams.set("eventId", eventId);
  url.searchParams.set("recipientUid", recipientUid);

  const call = await client.calls.create({
    to: phoneNumber,
    from: fromNumber,
    url: url.toString(),
    method: "POST",
    timeout: 20,
    timeLimit: MAX_CALL_DURATION_SECONDS
  });

  return {
    skipped: false,
    callSid: call.sid,
    status: call.status
  };
}

module.exports = {
  startEmergencyCall
};
