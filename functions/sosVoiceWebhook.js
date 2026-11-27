
"use strict";

const twilio = require("twilio");

const { onRequest } =
  require("firebase-functions/v2/https");

const { defineSecret } =
  require("firebase-functions/params");

const { getDatabase } =
  require("firebase-admin/database");

const { getVerifiedEmergencyPhone } =
  require("./sosPhoneSettings");

const TWILIO_AUTH_TOKEN =
  defineSecret("TWILIO_AUTH_TOKEN");

const TWILIO_ACCOUNT_SID =
  defineSecret("TWILIO_ACCOUNT_SID");

function validKey(value) {
  return (
    typeof value === "string" &&
    value.length > 0 &&
    value.length <= 128 &&
    !/[.#$\[\]/\u0000-\u001f\u007f]/.test(value)
  );
}

function validCallSid(value) {
  return (
    typeof value === "string" &&
    /^CA[a-fA-F0-9]{32}$/.test(value)
  );
}

exports.sosVoiceWebhook = onRequest(
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
      process.env.FUNCTIONS_EMULATOR === "true" ||
      process.env.SOS_LIVE_DELIVERY_ENABLED !== "true" ||
      process.env.SOS_VOICE_ENABLED !== "true"
    ) {
      return res.status(403)
        .send("Voice service disabled");
    }

    if (req.method !== "POST") {
      return res.status(405)
        .send("Method not allowed");
    }

    const webhookUrl =
      process.env.SOS_VOICE_WEBHOOK_URL;

    if (!webhookUrl) {
      return res.status(503)
        .send("Voice service unavailable");
    }

    const signature =
      req.get("X-Twilio-Signature") || "";

    const queryIndex =
      req.originalUrl.indexOf("?");

    const rawQuery = queryIndex >= 0
      ? req.originalUrl.substring(queryIndex)
      : "";

    const signedUrl =
      webhookUrl + rawQuery;

    const validSignature =
      twilio.validateRequest(
        TWILIO_AUTH_TOKEN.value(),
        signature,
        signedUrl,
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
      attemptId,
      action
    } = req.query;

    const roundNumber = Number(round);

    const callSid = req.body?.CallSid;
    const accountSid = req.body?.AccountSid;

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
      !validCallSid(callSid) ||
      accountSid !== TWILIO_ACCOUNT_SID.value() ||
      (
        action !== undefined &&
        action !== "acknowledge"
      )
    ) {
      return res.status(400)
        .send("Invalid call details");
    }

    const db = getDatabase();

    const attemptRef = db.ref(
      `sosCallAttempts/${senderUid}/${eventId}/${roundNumber}/${recipientUid}`
    );

    const ackRef = db.ref(
      `sosAcknowledgements/${senderUid}/${eventId}`
    );

    const [
      eventSnapshot,
      ackSnapshot,
      contactSnapshot,
      attemptSnapshot
    ] = await Promise.all([
      db.ref(
        `sosEvents/${senderUid}/${eventId}`
      ).get(),

      ackRef.get(),

      db.ref(
        `emergencyContacts/${senderUid}/${recipientUid}`
      ).get(),

      attemptRef.get()
    ]);

    const sos = eventSnapshot.val();
    const contact = contactSnapshot.val();
    const attempt = attemptSnapshot.val();

    const response =
      new twilio.twiml.VoiceResponse();

    function finish() {
      return res
        .type("text/xml")
        .send(response.toString());
    }

    function endCall(message) {
      response.say(message);
      response.hangup();
      return finish();
    }

    if (
      !attempt ||
      attempt.attemptId !== attemptId ||
      attempt.senderUid !== senderUid ||
      attempt.eventId !== eventId ||
      attempt.recipientUid !== recipientUid ||
      attempt.round !== roundNumber ||
      attempt.accountSid !== accountSid ||
      (
        attempt.callSid &&
        attempt.callSid !== callSid
      )
    ) {
      return endCall(
        "This emergency call could not be verified."
      );
    }

    const verifiedPhone =
      await getVerifiedEmergencyPhone(recipientUid)
        .catch(() => null);

    if (
      !verifiedPhone ||
      req.body?.To !== verifiedPhone
    ) {
      return endCall(
        "This emergency call is no longer authorized."
      );
    }

    const binding = await attemptRef.transaction(
      current => {

        if (
          !current ||
          current.attemptId !== attemptId ||
          current.accountSid !== accountSid ||
          (
            current.callSid &&
            current.callSid !== callSid
          )
        ) {
          return;
        }

        return {
          ...current,
          callSid,
          updatedAt: Date.now()
        };
      },
      undefined,
      false
    );

    if (!binding.committed) {
      return endCall(
        "This emergency call could not be verified."
      );
    }

    const boundAttempt = binding.snapshot.val();

    if (
      !sos ||
      sos.id !== eventId ||
      sos.userUid !== senderUid ||
      sos.status !== "active" ||
      ackSnapshot.exists() ||
      recipientUid === senderUid ||
      sos.recipientUids?.[recipientUid] !== true ||
      contact?.status !== "accepted" ||
      boundAttempt.cancellationRequested === true ||
      ![
        "creating",
        "submitted",
        "creation_unknown"
      ].includes(boundAttempt.status)
    ) {
      return endCall(
        "This emergency alert is no longer awaiting your response."
      );
    }

    if (action === "acknowledge") {

      if (String(req.body?.Digits || "") !== "1") {
        return endCall(
          "No acknowledgment was received."
        );
      }

      const latestEvent = await db.ref(
        `sosEvents/${senderUid}/${eventId}`
      ).get();

      if (latestEvent.val()?.status !== "active") {
        return endCall(
          "This emergency alert is no longer active."
        );
      }

      const result = await ackRef.transaction(
        current => {

          if (current !== null) return;

          return {
            acknowledgedBy: recipientUid,
            acknowledgedAt: Date.now(),
            method: "voice_call",
            callSid,
            attemptId
          };
        },
        undefined,
        false
      );

      if (result.committed) {

        return endCall(
          "Your emergency acknowledgment has been recorded. " +
          "Please open GLIMPSE to view the emergency details."
        );
      }

      return endCall(
        "This emergency has already been acknowledged."
      );
    }

    const acknowledgmentUrl =
      new URL(webhookUrl);

    acknowledgmentUrl.searchParams.set(
      "senderUid", senderUid
    );

    acknowledgmentUrl.searchParams.set(
      "eventId", eventId
    );

    acknowledgmentUrl.searchParams.set(
      "recipientUid", recipientUid
    );

    acknowledgmentUrl.searchParams.set(
      "round", String(roundNumber)
    );

    acknowledgmentUrl.searchParams.set(
      "attemptId", attemptId
    );

    acknowledgmentUrl.searchParams.set(
      "action", "acknowledge"
    );

    const gather = response.gather({
      input: "dtmf",
      numDigits: 1,
      timeout: 10,
      action: acknowledgmentUrl.toString(),
      method: "POST"
    });

    gather.say(
      "This is an emergency alert from GLIMPSE. " +
      "One of your trusted contacts has activated an SOS. " +
      "Please open GLIMPSE to view their location " +
      "and emergency details. " +
      "Press 1 to acknowledge this emergency."
    );

    response.say(
      "No acknowledgment was received."
    );

    response.hangup();

    return finish();
  }
);
