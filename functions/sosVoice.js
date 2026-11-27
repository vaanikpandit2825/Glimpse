
"use strict";

const twilio = require("twilio");

const { randomUUID } = require("node:crypto");

const { getDatabase } =
  require("firebase-admin/database");

const { getVerifiedEmergencyPhone } =
  require("./sosPhoneSettings");

const MAX_CALL_DURATION_SECONDS = 45;

function validKey(value) {
  return (
    typeof value === "string" &&
    value.length > 0 &&
    value.length <= 128 &&
    !/[.#$\[\]/\u0000-\u001f\u007f]/.test(value)
  );
}

function canPlaceCalls() {
  const stagingProject =
    process.env.SOS_STAGING_PROJECT_ID;

  return (
    Boolean(stagingProject) &&
    process.env.GCLOUD_PROJECT === stagingProject &&
    stagingProject !== "demo-glimpse" &&
    stagingProject !== "glimpse-e0aab" &&
    process.env.FUNCTIONS_EMULATOR !== "true" &&
    process.env.SOS_LIVE_DELIVERY_ENABLED === "true" &&
    process.env.SOS_VOICE_ENABLED === "true"
  );
}

async function isCallEligible({
  senderUid,
  eventId,
  recipientUid,
  phoneNumber
}) {
  const db = getDatabase();

  const [
    eventSnapshot,
    ackSnapshot,
    contactSnapshot
  ] = await Promise.all([
    db.ref(
      `sosEvents/${senderUid}/${eventId}`
    ).get(),

    db.ref(
      `sosAcknowledgements/${senderUid}/${eventId}`
    ).get(),

    db.ref(
      `emergencyContacts/${senderUid}/${recipientUid}`
    ).get()
  ]);

  const event = eventSnapshot.val();
  const contact = contactSnapshot.val();

  if (
    !event ||
    event.status !== "active" ||
    event.userUid !== senderUid ||
    event.id !== eventId ||
    ackSnapshot.exists() ||
    senderUid === recipientUid ||
    event.recipientUids?.[recipientUid] !== true ||
    contact?.status !== "accepted"
  ) {
    return false;
  }

  const verifiedPhone =
    await getVerifiedEmergencyPhone(recipientUid);

  return (
    Boolean(verifiedPhone) &&
    verifiedPhone === phoneNumber
  );
}

async function startEmergencyCall({
  senderUid,
  eventId,
  recipientUid,
  phoneNumber,
  round,
  accountSid,
  authToken,
  fromNumber,
  webhookBaseUrl
}) {
  if (!canPlaceCalls()) {
    return {
      skipped: true,
      reason: "voice_disabled"
    };
  }

  if (
    !validKey(senderUid) ||
    !validKey(eventId) ||
    !validKey(recipientUid) ||
    !Number.isInteger(round) ||
    round < 1 ||
    round > 5
  ) {
    return {
      skipped: true,
      reason: "invalid_call_details"
    };
  }

  if (
    typeof phoneNumber !== "string" ||
    !/^\+[1-9]\d{7,14}$/.test(phoneNumber)
  ) {
    throw new Error("Invalid phone number");
  }

  const callDetails = {
    senderUid,
    eventId,
    recipientUid,
    phoneNumber
  };

  if (!(await isCallEligible(callDetails))) {
    return {
      skipped: true,
      reason: "not_eligible"
    };
  }

  const url = new URL(webhookBaseUrl);

  if (
    url.protocol !== "https:" ||
    url.search ||
    url.hash
  ) {
    throw new Error("Invalid webhook URL");
  }

  const db = getDatabase();

  const attemptId = randomUUID();

  const attemptRef = db.ref(
    `sosCallAttempts/${senderUid}/${eventId}/${round}/${recipientUid}`
  );

  const claim = await attemptRef.transaction(
    current => {
      if (current !== null) return;

      return {
        attemptId,
        senderUid,
        eventId,
        recipientUid,
        round,
        accountSid,
        status: "creating",
        createdAt: Date.now()
      };
    },
    undefined,
    false
  );

  if (!claim.committed) {
    return {
      skipped: true,
      reason: "duplicate_attempt"
    };
  }

  if (!(await isCallEligible(callDetails))) {
    await attemptRef.update({
      status: "skipped",
      updatedAt: Date.now()
    });

    return {
      skipped: true,
      reason: "no_longer_eligible"
    };
  }

  url.searchParams.set("senderUid", senderUid);
  url.searchParams.set("eventId", eventId);
  url.searchParams.set("recipientUid", recipientUid);
  url.searchParams.set("round", String(round));
  url.searchParams.set("attemptId", attemptId);

const reservation = await reserveEmergencyCall({
  phoneNumber,
  attemptId
});

if (!reservation.allowed) {

  await attemptRef.update({
    status: "skipped",
    reason: reservation.reason,
    updatedAt: Date.now()
  });

  return {
    skipped: true,
    reason: reservation.reason
  };
}

if (!(await isCallEligible(callDetails))) {

  await attemptRef.update({
    status: "skipped",
    reason: "no_longer_eligible",
    updatedAt: Date.now()
  });

  return {
    skipped: true,
    reason: "no_longer_eligible"
  };
}

  const client = twilio(accountSid, authToken);

  const statusCallbackUrl = new URL(
    process.env.SOS_CALL_STATUS_URL
  );

  if (
    statusCallbackUrl.protocol !== "https:" ||
    statusCallbackUrl.search ||
    statusCallbackUrl.hash
  ) {
    throw new Error("Invalid status callback URL");
  }

  statusCallbackUrl.search = url.search;

  let call;

  try {
    call = await client.calls.create({
      to: phoneNumber,
      from: fromNumber,
      url: url.toString(),
      method: "POST",
      timeout: 20,
      timeLimit: MAX_CALL_DURATION_SECONDS,

      statusCallback: statusCallbackUrl.toString(),

      statusCallbackMethod: "POST",

      statusCallbackEvent: [
        "initiated",
        "ringing",
        "answered",
        "completed"
      ]
    });

  } catch (error) {

    await attemptRef.update({
      status: "creation_unknown",
      updatedAt: Date.now()
    });

    throw error;
  }

  await attemptRef.update({
    callSid: call.sid,
    providerStatus: call.status,
    updatedAt: Date.now()
  });

  if (!(await isCallEligible(callDetails))) {

    const cancelled = await stopTrackedCall({
      client,
      callSid: call.sid
    });

    await attemptRef.update({
      status: cancelled
        ? "stopped"
        : "stop_pending",

      updatedAt: Date.now()
    });

    return {
      skipped: false,
      callSid: call.sid,
      attemptId,
      status: cancelled
        ? "stopped"
        : "stop_pending"
    };
  }

  await attemptRef.transaction(current => {
    if (
      !current ||
      current.attemptId !== attemptId ||
      current.status !== "creating"
    ) {
      return;
    }

    return {
      ...current,
      status: "submitted",
      updatedAt: Date.now()
    };
  });

  return {
    skipped: false,
    callSid: call.sid,
    attemptId,
    status: call.status
  };
}

async function stopTrackedCall({
  client,
  callSid
}) {
  if (
    typeof callSid !== "string" ||
    !/^CA[a-fA-F0-9]{32}$/.test(callSid)
  ) {
    return false;
  }

  try {
    const call =
      await client.calls(callSid).fetch();

    const terminalStatuses = [
      "completed",
      "canceled",
      "failed",
      "busy",
      "no-answer"
    ];

    if (terminalStatuses.includes(call.status)) {
      return true;
    }

    const status =
      call.status === "in-progress"
        ? "completed"
        : "canceled";

    const updated =
      await client.calls(callSid).update({
        status
      });

    return terminalStatuses.includes(
      updated.status
    );

  } catch (error) {
    return false;
  }
}

module.exports = {
  startEmergencyCall,
  stopTrackedCall
};

const { reserveEmergencyCall } =
  require("./sosCallRateLimit");
