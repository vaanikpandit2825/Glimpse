
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

  const timestamp = { ".sv": "timestamp" };
  const path = "emergencyContacts/erin/frank";
  const inboxPath = "emergencyContactInbox/frank/erin";

  try {
    // Prepare fictional connection data.
    await env.withSecurityRulesDisabled(async context => {
      await context.database(url).ref().update({
        "connections/erin/frank": {
          status: "connected"
        },
        "connections/frank/erin": {
          status: "connected"
        },
        [path]: null,
        [inboxPath]: null
      });
    });

    const erin = env.authenticatedContext("erin")
      .database(url);

    const frank = env.authenticatedContext("frank")
      .database(url);

    const outsider = env.authenticatedContext("outsider")
      .database(url);

    // Test 1: Unconnected user cannot send a request.
    await assertFails(
      outsider.ref("emergencyContacts/outsider/zoe").set({
        requesterUid: "outsider",
        contactUid: "zoe",
        status: "pending",
        createdAt: timestamp,
        updatedAt: timestamp
      })
    );
    console.log("PASS: Unconnected request blocked");

    // Test 2: Fake inbox notification is blocked.
    await assertFails(
      erin.ref(inboxPath).set(true)
    );
    console.log("PASS: Fake emergency contact inbox blocked");

    // Test 3: Connected user can send a request.
    await assertSucceeds(
      erin.ref(path).set({
        requesterUid: "erin",
        contactUid: "frank",
        status: "pending",
        createdAt: timestamp,
        updatedAt: timestamp
      })
    );
    console.log("PASS: Connected user can request");


    // DEBUG: Check the records required by inbox rules.

    // Test 4: Real inbox notification can be created.
    await assertSucceeds(
      erin.ref(inboxPath).set(true)
    );
    console.log("PASS: Real inbox notification created");

    // Test 5: Recipient can read the notification.
    await assertSucceeds(
      frank.ref(inboxPath).get()
    );
    console.log("PASS: Recipient can read inbox notification");

    // Test 6: Requester cannot accept their own request.
    await assertFails(
      erin.ref(path).update({
        status: "accepted",
        updatedAt: timestamp
      })
    );
    console.log("PASS: Requester cannot self-accept");

    // Test 7: Intended recipient can read the request.
    await assertSucceeds(
      frank.ref(path).get()
    );
    console.log("PASS: Recipient can read request");

    // Test 8: Intended recipient can accept.
    await assertSucceeds(
      frank.ref(path).update({
        status: "accepted",
        updatedAt: timestamp
      })
    );
    console.log("PASS: Recipient can accept request");


    // Test 10: Recipient clears the inbox.
    await assertSucceeds(
      frank.ref(inboxPath).remove()
    );
    console.log("PASS: Recipient can clear inbox");

    // Test 11: Accepted request cannot create
    // another inbox notification.
    await assertFails(
      erin.ref(inboxPath).set(true)
    );
    console.log("PASS: Accepted request cannot recreate inbox");


    // Test 9: Outsider cannot read the request.
    await assertFails(
      outsider.ref(path).get()
    );
    console.log("PASS: Outsider cannot read request");

  } finally {
    await env.cleanup();
  }
}

runTests().catch(error => {
  console.error("TEST FAILED:", error);
  process.exitCode = 1;
});
