
"use strict";

const { createHash } =
  require("node:crypto");

const { getDatabase } =
  require("firebase-admin/database");

const MIN_CALL_INTERVAL_MS = 75 * 1000;

const WINDOW_MS = 15 * 60 * 1000;

const MAX_CALLS_PER_WINDOW = 5;

async function reserveEmergencyCall({
  phoneNumber,
  attemptId
}) {

  if (
    typeof phoneNumber !== "string" ||
    !/^\+[1-9]\d{7,14}$/.test(phoneNumber) ||
    typeof attemptId !== "string" ||
    !/^[a-f0-9-]{36}$/i.test(attemptId)
  ) {
    return {
      allowed: false,
      reason: "invalid_call_details"
    };
  }

  const phoneKey = createHash("sha256")
    .update(phoneNumber)
    .digest("hex");

  const rateRef = getDatabase().ref(
    `sosCallRateLimits/${phoneKey}`
  );

  const result = await rateRef.transaction(
    current => {

      const now = Date.now();

      const previous =
        current?.attempts || {};

      const recent = {};

      for (
        const [id, timestamp]
        of Object.entries(previous)
      ) {

        if (
          Number.isFinite(timestamp) &&
          timestamp > now - WINDOW_MS
        ) {
          recent[id] = timestamp;
        }
      }

      if (recent[attemptId] !== undefined) {
        return;
      }

      const timestamps =
        Object.values(recent);

      if (
        timestamps.length >= MAX_CALLS_PER_WINDOW
      ) {
        return;
      }

      const lastCall = timestamps.length
        ? Math.max(...timestamps)
        : 0;

      if (
        now - lastCall < MIN_CALL_INTERVAL_MS
      ) {
        return;
      }

      recent[attemptId] = now;

      return {
        attempts: recent,
        updatedAt: now
      };
    },
    undefined,
    false
  );

  return {
    allowed: result.committed,

    reason: result.committed
      ? "reserved"
      : "rate_limit_reached"
  };
}

module.exports = {
  reserveEmergencyCall
};
