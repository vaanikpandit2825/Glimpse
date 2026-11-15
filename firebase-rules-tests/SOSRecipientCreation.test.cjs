
const {
  initializeTestEnvironment
} = require("@firebase/rules-unit-testing");

async function runTest() {
  const env = await initializeTestEnvironment({
    projectId: "demo-glimpse",
    database: {
      host: "127.0.0.1",
      port: 9000
    }
  });

  const url =
    "https://demo-glimpse-default-rtdb.firebaseio.com";

  try {
    const alice = env.authenticatedContext("alice")
      .database(url);

    // Charlie is not an accepted emergency contact.
    const eventRef = alice.ref("sosEvents/alice").push();
    const now = Date.now();

    const event = {
      id: eventRef.key,
      userUid: "alice",
      createdAt: now,
      updatedAt: now,
      status: "active",
      recipientUids: {
        charlie: true
      }
    };

    try {
      await eventRef.set(event);

      console.log(
        "SECURITY GAP: Unapproved SOS recipient was accepted"
      );
    } catch (error) {
      if (error.code === "PERMISSION_DENIED") {
        console.log(
          "PASS: Unapproved SOS recipient was blocked"
        );
      } else {
        throw error;
      }
    }

  } finally {
    await env.cleanup();
  }
}

runTest().catch(error => {
  console.error("TEST ERROR:", error);
  process.exitCode = 1;
});
