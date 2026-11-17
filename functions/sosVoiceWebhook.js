
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

function validKey(value) {
  return (
    typeof value === "string" &&
    value.length > 0 &&
    value.length <= 128 &&
    !/[.#$\[\]/\u0000-\u001f\u007f]/.test(value)
  );
}

exports.sosVoiceWebhook = onRequest(
  {
    region: "us-central1",
    secrets: [TWILIO_AUTH_TOKEN]
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

    const signedUrl = webhookUrl + rawQuery;

    const validSignature = twilio.validateRequest(
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
      action
    } = req.query;

    if (
      !validKey(senderUid) ||
      !validKey(eventId) ||
      !validKey(recipientUid)
    ) {
      return res.status(400)
        .send("Invalid SOS details");
    }

    const db = getDatabase();

    const ackRef = db.ref(
      `sosAcknowledgements/${senderUid}/${eventId}`
    );

    const [
      eventSnapshot,
      ackSnapshot,
      contactSnapshot
    ] = await Promise.all([
      db.ref(
        `sosEvents/${senderUid}/${eventId}`
      ).get(),

      ackRef.get(),

      db.ref(
        `emergencyContacts/${senderUid}/${recipientUid}`
      ).get()
    ]);

    const sos = eventSnapshot.val();

    const contact = contactSnapshot.val();

    const verifiedPhone =
      await getVerifiedEmergencyPhone(recipientUid)
        .catch(() => null);

    const response =
      new twilio.twiml.VoiceResponse();

    function finish() {
      return res
        .type("text/xml")
        .send(response.toString());
    }

    if (
      !sos ||
      sos.id !== eventId ||
      sos.userUid !== senderUid ||
      sos.status !== "active" ||
      ackSnapshot.exists() ||
      recipientUid === senderUid ||
      sos.recipientUids?.[recipientUid] !== true ||
      contact?.status !== "accepted" ||
      !verifiedPhone ||
      req.body?.To !== verifiedPhone
    ) {
      response.say(
        "This emergency alert is no longer awaiting your response."
      );

      response.hangup();

      return finish();
    }

    if (action === "acknowledge") {

      if (String(req.body?.Digits || "") === "1") {

        const result = await ackRef.transaction(
          current => {

            if (current !== null) return;

            return {
              acknowledgedBy: recipientUid,
              acknowledgedAt: Date.now(),
              method: "voice_call"
            };
          },
          undefined,
          false
        );

        if (result.committed) {

          response.say(
            "Your emergency acknowledgment has been recorded. " +
            "Please open GLIMPSE to view the emergency details."
          );

        } else {

          response.say(
            "This emergency has already been acknowledged."
          );
        }

        response.hangup();

        return finish();
      }

      response.say(
        "No acknowledgment was received."
      );

      response.hangup();

      return finish();
    }

    const acknowledgmentUrl =
      new URL(webhookUrl);

    acknowledgmentUrl.searchParams.set(
      "senderUid",
      senderUid
    );

    acknowledgmentUrl.searchParams.set(
      "eventId",
      eventId
    );

    acknowledgmentUrl.searchParams.set(
      "recipientUid",
      recipientUid
    );

    acknowledgmentUrl.searchParams.set(
      "action",
      "acknowledge"
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
