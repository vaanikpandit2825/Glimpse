
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
    // Prepare fictional group data.
    await env.withSecurityRulesDisabled(async context => {
      await context.database(url).ref().update({
        "groups/delete-test": {
          id: "delete-test",
          createdBy: "alice",
          createdAt: 123456789,
          name: "Test Group",
          type: "Friends",
          members: {
            alice: true,
            bob: true
          }
        },
        "userGroups/alice/delete-test": true,
        "userGroups/bob/delete-test": true
      });
    });

    const alice = env.authenticatedContext("alice")
      .database(url);

    const bob = env.authenticatedContext("bob")
      .database(url);

    const updates = {
      "groups/delete-test": null,
      "userGroups/alice/delete-test": null,
      "userGroups/bob/delete-test": null
    };

    // Non-creator cannot delete.
    await assertFails(bob.ref().update(updates));
    console.log("PASS: Non-creator cannot delete group");

    // Creator can delete group and indexes.
    await assertSucceeds(alice.ref().update(updates));
    console.log("PASS: Creator can delete group");

    // Verify nothing remains.
    await env.withSecurityRulesDisabled(async context => {
      const db = context.database(url);

      for (const path of Object.keys(updates)) {
        const snapshot = await db.ref(path).get();

        if (snapshot.exists()) {
          throw new Error("Data remains at: " + path);
        }
      }
    });

    console.log("PASS: Group and indexes removed");

  } finally {
    await env.cleanup();
  }
}

runTests().catch(error => {
  console.error("TEST FAILED:", error);
  process.exitCode = 1;
});
