
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails
} = require("@firebase/rules-unit-testing");

async function runTests() {
  const env = await initializeTestEnvironment({
    projectId: "demo-glimpse",
    database: {
      host: "127.0.0.1",
      port: 9000
    }
  });

  const url =
    "https://demo-glimpse-default-rtdb.firebaseio.com";

  const eventPath = "sosEvents/alice/test-sos-event";

  try {
    // Create fictional test data.
    await env.withSecurityRulesDisabled(async context => {
      await context.database(url).ref(eventPath).set({
        id: "test-sos-event",
        userUid: "alice",
        createdAt: Date.now() - 10000,
        updatedAt: Date.now() - 10000,
        status: "active",
        recipientUids: { bob: true }
      });
    });

    const alice = env.authenticatedContext("alice")
      .database(url);
    const bob = env.authenticatedContext("bob")
      .database(url);
    const charlie = env.authenticatedContext("charlie")
      .database(url);

    await assertSucceeds(bob.ref(eventPath).get());
    console.log("PASS: SOS recipient can read event");

    await assertFails(charlie.ref(eventPath).get());
    console.log("PASS: Non-recipient cannot read event");

    await assertFails(
      bob.ref(eventPath + "/status").set("resolved")
    );
    console.log("PASS: Recipient cannot change SOS status");

    await assertFails(
      alice.ref(eventPath + "/recipientUids/charlie")
        .set(true)
    );
    console.log("PASS: Recipients cannot be changed");

    await assertSucceeds(
      alice.ref(eventPath).update({
        status: "resolved",
        updatedAt: Date.now()
      })
    );
    console.log("PASS: Owner can resolve an active SOS");


await assertFails(
  alice.ref(eventPath).update({
    status: "active",
    updatedAt: Date.now() + 1000
  })
);

console.log("PASS: Resolved SOS cannot be reopened");


const cancelledPath = "sosEvents/alice/cancel-test";
const createdAt = Date.now() - 10000;

// Set up a fictional active SOS event.
await env.withSecurityRulesDisabled(async context => {
  await context.database(url).ref(cancelledPath).set({
    id: "cancel-test",
    userUid: "alice",
    createdAt: createdAt,
    updatedAt: createdAt,
    status: "active",
    recipientUids: { bob: true }
  });
});

// Owner can cancel an active SOS.
await assertSucceeds(
  alice.ref(cancelledPath).update({
    status: "cancelled",
    updatedAt: Date.now()
  })
);
console.log("PASS: Owner can cancel active SOS");

// Cancelled SOS cannot be reopened.
await assertFails(
  alice.ref(cancelledPath).update({
    status: "active",
    updatedAt: Date.now() + 1000
  })
);
console.log("PASS: Cancelled SOS cannot be reopened");


  } finally {
    await env.cleanup();
  }
}

runTests().catch(error => {
  console.error("TEST FAILED:", error);
  process.exitCode = 1;
});
