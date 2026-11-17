
"use strict";

const { onCall, HttpsError } =
  require("firebase-functions/v2/https");

const { getAuth } =
  require("firebase-admin/auth");

const { getDatabase, ServerValue } =
  require("firebase-admin/database");

function requireLocalEnvironment() {
  if (
    process.env.FUNCTIONS_EMULATOR !== "true" ||
    process.env.GCLOUD_PROJECT !== "demo-glimpse" ||
    !process.env.FIREBASE_DATABASE_EMULATOR_HOST
  ) {
    throw new HttpsError(
      "failed-precondition",
      "Emergency phone settings are not enabled here."
    );
  }
}

// Retrieve the current user's emergency call settings.
exports.getSosPhoneSettings = onCall(
  { region: "us-central1" },
  async request => {

    requireLocalEnvironment();

    const uid = request.auth?.uid;

    if (!uid) {
      throw new HttpsError(
        "unauthenticated",
        "Please log in."
      );
    }

    const user = await getAuth().getUser(uid);

    const snapshot = await getDatabase()
      .ref(`sosPhoneSettings/${uid}`)
      .get();

    const settings = snapshot.val() || {};

    const phoneNumber = user.phoneNumber || "";

    const consentMatchesNumber =
      settings.consentedPhoneNumber === phoneNumber;

    return {
      verified: Boolean(phoneNumber),

      maskedPhoneNumber: phoneNumber
        ? `******${phoneNumber.slice(-4)}`
        : "",

      voiceCallConsent:
        Boolean(phoneNumber) &&
        consentMatchesNumber &&
        settings.voiceCallConsent === true
    };
  }
);

// Enable or disable automated emergency calls.
exports.setSosVoiceConsent = onCall(
  { region: "us-central1" },
  async request => {

    requireLocalEnvironment();

    const uid = request.auth?.uid;

    if (!uid) {
      throw new HttpsError(
        "unauthenticated",
        "Please log in."
      );
    }

    const enabled = request.data?.enabled;

    if (typeof enabled !== "boolean") {
      throw new HttpsError(
        "invalid-argument",
        "Consent must be true or false."
      );
    }

    const user = await getAuth().getUser(uid);

    if (enabled && !user.phoneNumber) {
      throw new HttpsError(
        "failed-precondition",
        "Verify your phone number first."
      );
    }

    await getDatabase()
      .ref(`sosPhoneSettings/${uid}`)
      .set({
        voiceCallConsent: enabled,

        consentedPhoneNumber:
          enabled ? user.phoneNumber : null,

        updatedAt: ServerValue.TIMESTAMP
      });

    return {
      success: true,
      voiceCallConsent: enabled
    };
  }
);

// Server-only helper for the SOS calling engine.
async function getVerifiedEmergencyPhone(uid) {

  const user = await getAuth().getUser(uid);

  if (!user.phoneNumber || user.disabled) {
    return null;
  }

  const snapshot = await getDatabase()
    .ref(`sosPhoneSettings/${uid}`)
    .get();

  const settings = snapshot.val() || {};

  if (
    settings.voiceCallConsent !== true ||
    settings.consentedPhoneNumber !== user.phoneNumber
  ) {
    return null;
  }

  return user.phoneNumber;
}

exports.getVerifiedEmergencyPhone =
  getVerifiedEmergencyPhone;
