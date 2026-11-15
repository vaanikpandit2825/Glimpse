
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

  try {
    // Create a fictional accepted emergency contact.
    await env.withSecurityRulesDisabled(async context => {
      await context.database(url)
        .ref("emergencyContacts/alice/bob")
        .set({
          requesterUid: "alice",
          contactUid: "bob",
          status: "accepted",
          createdAt: Date.now(),
          updatedAt: Date.now()
        });
    });

    const alice = env.authenticatedContext("alice")
      .database(url);

    // Test 1: Accepted contact Bob should be allowed.
    const validRef = alice.ref("sosEvents/alice").push();
    const now = Date.now();

    await assertSucceeds(validRef.set({
      id: validRef.key,
      userUid: "alice",
      createdAt: now,
      updatedAt: now,
      status: "active",
      recipientUids: {
        bob: true
      }
    }));

    console.log("PASS: Accepted emergency contact allowed");

    // Test 2: Adding unapproved Charlie should
    // reject the entire event.
    const mixedRef = alice.ref("sosEvents/alice").push();
    const now2 = Date.now();

    await assertFails(mixedRef.set({
      id: mixedRef.key,
      userUid: "alice",
      createdAt: now2,
      updatedAt: now2,
      status: "active",
      recipientUids: {
        bob: true,
        charlie: true
      }
    }));

    console.log("PASS: Mixed recipient list rejected");


const emptyRef = alice.ref("sosEvents/alice").push();
const now3 = Date.now();

await assertFails(emptyRef.set({
  id: emptyRef.key,
  userUid: "alice",
  createdAt: now3,
  updatedAt: now3,
  status: "active"
}));

console.log("PASS: SOS without recipients blocked");


  } finally {
    await env.cleanup();
  }
}

runTests().catch(error => {
  console.error("TEST FAILED:", error);
  process.exitCode = 1;
});
