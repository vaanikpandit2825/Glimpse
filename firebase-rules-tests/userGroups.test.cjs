
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails
} = require("@firebase/rules-unit-testing");

async function runTests() {
  const testEnv = await initializeTestEnvironment({
    projectId: "demo-glimpse",
    database: {
      host: "127.0.0.1",
      port: 9000
    }
  });

  const databaseUrl =
    "https://demo-glimpse-default-rtdb.firebaseio.com";

  try {
    // Create a fictional group for testing.
    // This setup bypasses rules only for test data.
    await testEnv.withSecurityRulesDisabled(async context => {
      const db = context.database(databaseUrl);

      await db.ref("groups/test-group").set({
        id: "test-group",
        createdBy: "alice",
        createdAt: 123456789,
        name: "Test Group",
        type: "Friends",
        members: {
          alice: true,
          bob: true
        }
      });
    });

    const aliceDb = testEnv
      .authenticatedContext("alice")
      .database(databaseUrl);

    const bobDb = testEnv
      .authenticatedContext("bob")
      .database(databaseUrl);

    // Test 1: Creator indexes an actual member.
    await assertSucceeds(
      aliceDb.ref("userGroups/bob/test-group").set(true)
    );
    console.log("PASS: Creator can index a real member");

    // Test 2: Member tries to delete an index.
    await assertFails(
      bobDb.ref("userGroups/bob/test-group").remove()
    );
    console.log("PASS: Non-creator cannot delete the index");

    // Test 3: Creator tries to index a non-member.
    await assertFails(
      aliceDb.ref("userGroups/charlie/test-group").set(true)
    );
    console.log("PASS: Cannot index a non-member");

    // Test 4: Bob tries to create Alice's index.
    await assertFails(
      bobDb.ref("userGroups/alice/test-group").set(true)
    );
    console.log("PASS: Non-creator cannot forge an index");

  } finally {
    await testEnv.cleanup();
  }
}

runTests().catch(error => {
  console.error("TEST FAILED:", error);
  process.exitCode = 1;
});
