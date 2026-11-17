
"use strict";

const { onCall, HttpsError } =
  require("firebase-functions/v2/https");

const { getDatabase } =
  require("firebase-admin/database");

exports.acknowledgeSos = onCall(
  { region: "us-central1" },
  async (request) => {
    // Keep this implementation local-only for now.
    if (
      process.env.FUNCTIONS_EMULATOR !== "true" ||
      process.env.GCLOUD_PROJECT !== "demo-glimpse" ||
      !process.env.FIREBASE_DATABASE_EMULATOR_HOST
    ) {
      throw new HttpsError(
        "failed-precondition",
        "SOS acknowledgment is not enabled in this environment."
      );
    }

    const recipientUid = request.auth?.uid;

    if (!recipientUid) {
      throw new HttpsError(
        "unauthenticated",
        "You must log in to acknowledge an SOS."
      );
    }

    const { senderUid, eventId } = request.data || {};

    const validKey = (value) =>
      typeof value === "string" &&
      value.length > 0 &&
      value.length <= 128 &&
      !/[.#$\[\]/\u0000-\u001f\u007f]/.test(value);

    if (!validKey(senderUid) || !validKey(eventId)) {
      throw new HttpsError(
        "invalid-argument",
        "Valid senderUid and eventId are required."
      );
    }

    const db = getDatabase();

    const [eventSnapshot, contactSnapshot] = await Promise.all([
      db.ref(`sosEvents/${senderUid}/${eventId}`).get(),
      db.ref(
        `emergencyContacts/${senderUid}/${recipientUid}`
      ).get()
    ]);

    const sos = eventSnapshot.val();
    const contact = contactSnapshot.val();

    if (
      !sos ||
      sos.userUid !== senderUid ||
      sos.id !== eventId ||
      sos.status !== "active"
    ) {
      throw new HttpsError(
        "failed-precondition",
        "This SOS is no longer active."
      );
    }

    if (
      recipientUid === senderUid ||
      sos.recipientUids?.[recipientUid] !== true ||
      contact?.status !== "accepted"
    ) {
      throw new HttpsError(
        "permission-denied",
        "You are not an approved SOS recipient."
      );
    }

    const ackRef = db.ref(
      `sosAcknowledgements/${senderUid}/${eventId}`
    );

    // Only the first acknowledgment wins.
    const result = await ackRef.transaction(
      (current) => {
        if (current !== null) return;

        return {
          acknowledgedBy: recipientUid,
          acknowledgedAt: Date.now(),
          method: "in_app"
        };
      },
      undefined,
      false
    );

    const acknowledgment = result.snapshot.val();

    return {
      acknowledged: Boolean(acknowledgment),
      acknowledgedBy: acknowledgment?.acknowledgedBy || null,
      firstResponder: result.committed
    };
  }
);
