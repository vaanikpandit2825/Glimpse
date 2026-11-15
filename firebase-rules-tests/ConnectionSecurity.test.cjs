
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

  const sender = "test_sender";
  const recipient = "test_recipient";

  const requestPath =
    `connectionRequests/${recipient}/${sender}`;

  const connectionPath =
    `connections/${recipient}/${sender}`;

  const locationPath = `locations/${recipient}`;

  const flags = {
    location: false,
    profile: false,
    locationHistory: false
  };

  try {
    // Prepare fictional emulator data.
    await env.withSecurityRulesDisabled(async context => {
      await context.database(url).ref().update({
        [requestPath]: null,
        [connectionPath]: null,
        [`connections/${sender}/${recipient}`]: null,
        [locationPath]: {
          latitude: 12.9,
          longitude: 80.2,
          timestamp: Date.now()
        }
      });
    });

    const alice = env
      .authenticatedContext(sender)
      .database(url);

    const recipientDb = env
      .authenticatedContext(recipient)
      .database(url);

    // Test 1: Stranger cannot read location.
    await assertFails(
      alice.ref(locationPath).get()
    );
    console.log("PASS: Stranger cannot read location");

    // Test 2: Sender can create a connection request.
    await assertSucceeds(
      alice.ref(requestPath).set({
        status: "pending",
        createdAt: Date.now(),
        senderSharing: flags
      })
    );
    console.log("PASS: Connection request created");

    // Test 3: Pending request grants no location access.
    await assertFails(
      alice.ref(locationPath).get()
    );
    console.log("PASS: Pending request cannot read location");

    // Test 4: Sender cannot forge a connection.
    await assertFails(
      alice.ref(connectionPath).set({
        status: "connected",
        connectedAt: Date.now(),
        sharing: {
          location: true,
          profile: true,
          locationHistory: true
        },
        partnerSharing: flags
      })
    );
    console.log("PASS: Requester cannot forge connection");

    // Test 5: Recipient can accept the request.
    await assertSucceeds(
      recipientDb.ref(connectionPath).set({
        status: "connected",
        connectedAt: Date.now(),
        sharing: flags,
        partnerSharing: flags
      })
    );
    console.log("PASS: Recipient can accept connection");

    // Test 6: Location remains private.
    await assertFails(
      alice.ref(locationPath).get()
    );
    console.log(
      "PASS: Accepted connection respects location privacy"
    );

    // Test 7: Recipient creates the matching
    // connection record for the sender.
    await assertSucceeds(
      recipientDb
        .ref(`connections/${sender}/${recipient}`)
        .set({
          status: "connected",
          connectedAt: Date.now(),
          sharing: flags,
          partnerSharing: flags
        })
    );

    console.log("PASS: Two-way connection created");


  } finally {
    await env.cleanup();
  }
}

runTests().catch(error => {
  console.error("TEST FAILED:", error);
  process.exitCode = 1;
});
