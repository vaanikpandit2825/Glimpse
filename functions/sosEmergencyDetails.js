
"use strict";

const { getDatabase } =
  require("firebase-admin/database");

const { onValueWritten } =
  require("firebase-functions/v2/database");

const MAX_LOCATION_AGE_MS = 120000;

function environmentAllowed() {

  const project = process.env.GCLOUD_PROJECT;

  if (
    project === "demo-glimpse" &&
    process.env.FUNCTIONS_EMULATOR === "true" &&
    process.env.FIREBASE_DATABASE_EMULATOR_HOST
  ) {
    return true;
  }

  return Boolean(
    process.env.SOS_STAGING_PROJECT_ID &&
    project === process.env.SOS_STAGING_PROJECT_ID &&
    project !== "demo-glimpse" &&
    project !== "glimpse-e0aab" &&
    process.env.FUNCTIONS_EMULATOR !== "true"
  );
}

function validLocation(location) {

  return Boolean(
    location &&
    Number.isFinite(location.latitude) &&
    Number.isFinite(location.longitude) &&
    Number.isFinite(location.timestamp) &&
    location.latitude >= -90 &&
    location.latitude <= 90 &&
    location.longitude >= -180 &&
    location.longitude <= 180 &&
    location.timestamp > 0 &&
    location.timestamp <= Date.now() + 30000
  );
}

async function publishEmergencyDetails(
  senderUid,
  eventId
) {

  if (!environmentAllowed()) return;

  const db = getDatabase();

  const eventRef = db.ref(
    `sosEvents/${senderUid}/${eventId}`
  );

  const detailsRef = db.ref(
    `sosEmergencyDetails/${senderUid}/${eventId}`
  );

  const [
    eventSnapshot,
    profileSnapshot,
    locationSnapshot
  ] = await Promise.all([

    eventRef.get(),

    db.ref(`profiles/${senderUid}`).get(),

    db.ref(`locations/${senderUid}`).get()
  ]);

  const sos = eventSnapshot.val();

  if (
    !sos ||
    sos.id !== eventId ||
    sos.userUid !== senderUid ||
    sos.status !== "active"
  ) {
    await detailsRef.remove();
    return;
  }

  const profile = profileSnapshot.val() || {};
  const location = locationSnapshot.val();

  const name =
    typeof profile.name === "string"
      ? profile.name.slice(0, 80)
      : "";

  const glimpseId =
    typeof profile.glimpseId === "string"
      ? profile.glimpseId.slice(0, 8)
      : "";

  const profilePhotoUrl =
    typeof profile.profilePhotoUrl === "string" &&
    profile.profilePhotoUrl.startsWith("https://")
      ? profile.profilePhotoUrl.slice(0, 2048)
      : "";

  const latestEvent = await eventRef.get();

  if (latestEvent.val()?.status !== "active") {
    await detailsRef.remove();
    return;
  }

  await detailsRef.transaction(current => {

    const details = {
      senderUid,
      eventId,
      name,
      glimpseId,
      profilePhotoUrl,
      updatedAt: Date.now()
    };

    if (validLocation(location)) {

      const previous =
        current?.latestLocation;

      if (
        previous &&
        previous.timestamp > location.timestamp
      ) {

        details.latestLocation = previous;

      } else {

        details.latestLocation = {
          latitude: location.latitude,
          longitude: location.longitude,
          timestamp: location.timestamp
        };
      }

    } else if (current?.latestLocation) {

      details.latestLocation =
        current.latestLocation;
    }

    details.locationFreshAtPublish = Boolean(
      details.latestLocation &&
      Date.now() -
        details.latestLocation.timestamp <=
        MAX_LOCATION_AGE_MS
    );

    return details;
  });

  const finalEvent = await eventRef.get();

  if (finalEvent.val()?.status !== "active") {
    await detailsRef.remove();
  }
}

function createEmergencyDetailsTriggers({
  instance,
  region
}) {

  const onSosDetailsChanged = onValueWritten(
    {
      ref: "/sosEvents/{senderUid}/{eventId}",
      instance,
      region
    },

    async event => {

      if (!environmentAllowed()) return;

      const { senderUid, eventId } =
        event.params;

      await publishEmergencyDetails(
        senderUid,
        eventId
      );
    }
  );

  const onSosLocationChanged = onValueWritten(
    {
      ref: "/locations/{senderUid}",
      instance,
      region
    },

    async event => {

      if (!environmentAllowed()) return;

      const { senderUid } = event.params;

      const snapshot = await getDatabase()
        .ref(`sosEvents/${senderUid}`)
        .orderByChild("status")
        .equalTo("active")
        .get();

      for (const child of snapshot.children || []) {
        if (!child) continue;
      }

      const activeEvents = [];

      snapshot.forEach(child => {

        const sos = child.val();

        if (
          typeof child.key === "string" &&
          /^[A-Za-z0-9_-]{1,128}$/.test(child.key) &&
          sos?.id === child.key &&
          sos?.userUid === senderUid
        ) {
          activeEvents.push(child.key);
        }
      });

      for (const eventId of activeEvents) {

        await publishEmergencyDetails(
          senderUid,
          eventId
        );
      }
    }
  );

  return {
    onSosDetailsChanged,
    onSosLocationChanged
  };
}

module.exports = {
  createEmergencyDetailsTriggers
};
