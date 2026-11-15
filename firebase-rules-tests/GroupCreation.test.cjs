
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

  const groupId = `create-test-${Date.now()}`;

  try {
    const alice = env.authenticatedContext("alice")
      .database(url);

    const bob = env.authenticatedContext("bob")
      .database(url);

    const group = {
      id: groupId,
      createdBy: "alice",
      createdAt: Date.now(),
      name: "Test Group",
      type: "Friends",
      members: {
        alice: true
      }
    };

    // Test 1: Creator can create a group.
    await assertSucceeds(
      alice.ref(`groups/${groupId}`).set(group)
    );
    console.log("PASS: Creator can create group");

    // Test 2: Creator can add their group index.
    await assertSucceeds(
      alice.ref(`userGroups/alice/${groupId}`).set(true)
    );
    console.log("PASS: Group index created");

    // Test 3: Other users cannot modify the group.
    await assertFails(
      bob.ref(`groups/${groupId}/name`).set("Hacked")
    );
    console.log("PASS: Non-creator cannot edit group");

    // Test 4: Non-member cannot forge an index.
    await assertFails(
      bob.ref(`userGroups/bob/${groupId}`).set(true)
    );
    console.log("PASS: Non-member cannot forge index");


    // Test 5: Bob cannot add himself.
    await assertFails(
      bob.ref(`groups/${groupId}/members/bob`).set(true)
    );
    console.log("PASS: Non-creator cannot add himself");


    // Create a two-way connection for Alice and Bob.
    // Charlie remains unconnected.
    await env.withSecurityRulesDisabled(async context => {
      await context.database(url).ref().update({
        "connections/alice/bob": {
          status: "connected"
        },
        "connections/bob/alice": {
          status: "connected"
        }
      });
    });

    console.log("SETUP: Alice and Bob are connected");


    // Test 6: Alice adds Bob to the group.
    await assertSucceeds(
      alice.ref(`groups/${groupId}/members/bob`).set(true)
    );
    console.log("PASS: Creator can add member");

    // Test 7: Alice creates Bob's membership index.
    await assertSucceeds(
      alice.ref(`userGroups/bob/${groupId}`).set(true)
    );
    console.log("PASS: New member index created");

    // Test 8: Alice removes both records atomically.
    await assertSucceeds(
      alice.ref().update({
        [`groups/${groupId}/members/bob`]: null,
        [`userGroups/bob/${groupId}`]: null
      })
    );
    console.log("PASS: Creator can remove member and index");


    // Test 9: Can Alice add Charlie without a connection?
    // Charlie has no connection or invitation.

    try {
      await alice
        .ref(`groups/${groupId}/members/charlie`)
        .set(true);

      console.log(
        "SECURITY GAP: Unconnected user was added to group"
      );

    } catch (error) {
      if (error.code === "PERMISSION_DENIED") {
        console.log(
          "PASS: Unconnected user cannot be added"
        );
      } else {
        throw error;
      }
    }


  } finally {
    await env.cleanup();
  }
}

runTests().catch(error => {
  console.error("TEST FAILED:", error);
  process.exitCode = 1;
});
