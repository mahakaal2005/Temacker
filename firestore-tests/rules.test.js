const fs = require("fs");
const path = require("path");
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails
} = require("@firebase/rules-unit-testing");

const PROJECT_ID = "demo-temacker";

const OWNER_UID = "owner-uid";
const LEADER_UID = "owner-uid"; // creator == Leader in this app
const MEMBER_UID = "member-uid";
const OUTSIDER_UID = "outsider-uid";

const PROJECT_ID_1 = "project-1";
const LEADER_ROLE_ID = "role-leader";
const DEFAULT_ROLE_ID = "role-default";

function fullPermissions() {
  return {
    manageRoles: true,
    manageInviteCode: true,
    removeMembers: true,
    deleteProject: true,
    assignTasks: true,
    editAnyTask: true,
    manageTags: true
  };
}

function noPermissions() {
  return {
    manageRoles: false,
    manageInviteCode: false,
    removeMembers: false,
    deleteProject: false,
    assignTasks: false,
    editAnyTask: false,
    manageTags: false
  };
}

function managerPermissions() {
  return { ...noPermissions(), manageRoles: true, manageInviteCode: true, removeMembers: true };
}

let testEnv;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync(path.resolve(__dirname, "../firestore.rules"), "utf8"),
      host: "127.0.0.1",
      port: 8080
    }
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

afterEach(async () => {
  await testEnv.clearFirestore();
});

// Seeds project-1 (owned by OWNER_UID/LEADER_UID) with a Leader role, a Default (no-permission)
// role, a Manager role (manageRoles/manageInviteCode/removeMembers), and members for LEADER_UID
// (Leader) and MEMBER_UID (Default), bypassing rules.
async function seedProjectOne() {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("projects").doc(PROJECT_ID_1).set({
      name: "Project One",
      ownerUid: OWNER_UID,
      createdAt: Date.now(),
      isArchived: false,
      predecessorProjectId: null
    });
    await db.collection(`projects/${PROJECT_ID_1}/roles`).doc(LEADER_ROLE_ID).set({
      name: "Leader",
      permissions: fullPermissions(),
      isLeader: true
    });
    await db.collection(`projects/${PROJECT_ID_1}/roles`).doc(DEFAULT_ROLE_ID).set({
      name: "Default",
      permissions: noPermissions(),
      isLeader: false
    });
    await db.collection(`projects/${PROJECT_ID_1}/roles`).doc("role-manager").set({
      name: "Manager",
      permissions: managerPermissions(),
      isLeader: false
    });
    await db.collection(`projects/${PROJECT_ID_1}/members`).doc(LEADER_UID).set({
      userId: LEADER_UID,
      roleId: LEADER_ROLE_ID,
      roleName: "Leader",
      permissions: fullPermissions(),
      displayName: "Leader Person",
      photoUrl: null,
      joinedAt: Date.now(),
      isLeader: true
    });
    await db.collection(`projects/${PROJECT_ID_1}/members`).doc(MEMBER_UID).set({
      userId: MEMBER_UID,
      roleId: DEFAULT_ROLE_ID,
      roleName: "Default",
      permissions: noPermissions(),
      displayName: "Regular Member",
      photoUrl: null,
      joinedAt: Date.now(),
      isLeader: false
    });
  });
}

function asLeader() {
  return testEnv.authenticatedContext(LEADER_UID).firestore();
}
function asMember() {
  return testEnv.authenticatedContext(MEMBER_UID).firestore();
}
function asOutsider() {
  return testEnv.authenticatedContext(OUTSIDER_UID).firestore();
}
function asUnauthenticated() {
  return testEnv.unauthenticatedContext().firestore();
}

describe("projects/{projectId}", () => {
  beforeEach(seedProjectOne);

  test("member can read the project doc", async () => {
    await assertSucceeds(asLeader().collection("projects").doc(PROJECT_ID_1).get());
  });

  test("signed-in non-member CAN get the project doc by known ID (needed for joinProject's transaction)", async () => {
    await assertSucceeds(asOutsider().collection("projects").doc(PROJECT_ID_1).get());
  });

  test("non-member cannot list/query projects", async () => {
    await assertFails(asOutsider().collection("projects").where("ownerUid", "==", LEADER_UID).get());
  });

  test("unauthenticated cannot read the project doc", async () => {
    await assertFails(asUnauthenticated().collection("projects").doc(PROJECT_ID_1).get());
  });

  test("signed-in user can create a new project doc with ownerUid == self", async () => {
    await assertSucceeds(
      asOutsider()
        .collection("projects")
        .doc("new-project")
        .set({ name: "New Project", ownerUid: OUTSIDER_UID, createdAt: Date.now() })
    );
  });

  test("cannot create a project doc with a spoofed ownerUid", async () => {
    await assertFails(
      asOutsider()
        .collection("projects")
        .doc("new-project-2")
        .set({ name: "New Project", ownerUid: LEADER_UID, createdAt: Date.now() })
    );
  });

  test("cannot create a project doc that already exists", async () => {
    await assertFails(
      asLeader()
        .collection("projects")
        .doc(PROJECT_ID_1)
        .set({ name: "Project One", ownerUid: LEADER_UID, createdAt: Date.now() })
    );
  });

  test("updating any field other than isArchived is always denied, even for the Leader", async () => {
    await assertFails(
      asLeader().collection("projects").doc(PROJECT_ID_1).update({ name: "Renamed" })
    );
  });

  test("the Leader can archive the project (succession)", async () => {
    await assertSucceeds(
      asLeader().collection("projects").doc(PROJECT_ID_1).update({ isArchived: true })
    );
  });

  test("a non-Leader member cannot archive the project", async () => {
    await assertFails(
      asMember().collection("projects").doc(PROJECT_ID_1).update({ isArchived: true })
    );
  });

  test("archiving cannot be bundled with another field change", async () => {
    await assertFails(
      asLeader().collection("projects").doc(PROJECT_ID_1).update({ isArchived: true, name: "Renamed" })
    );
  });

  test("un-archiving is denied", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.firestore().collection("projects").doc(PROJECT_ID_1).update({ isArchived: true });
    });
    await assertFails(
      asLeader().collection("projects").doc(PROJECT_ID_1).update({ isArchived: false })
    );
  });

  test("delete on an existing project is always denied, even for the owner", async () => {
    await assertFails(asLeader().collection("projects").doc(PROJECT_ID_1).delete());
  });
});

describe("projects/{projectId}/roles/{roleId}", () => {
  beforeEach(seedProjectOne);

  test("member can read roles", async () => {
    await assertSucceeds(
      asMember().collection(`projects/${PROJECT_ID_1}/roles`).doc(LEADER_ROLE_ID).get()
    );
  });

  test("signed-in non-member CAN get a role by known ID (needed for joinProject's transaction)", async () => {
    await assertSucceeds(
      asOutsider().collection(`projects/${PROJECT_ID_1}/roles`).doc(LEADER_ROLE_ID).get()
    );
  });

  test("non-member cannot list/query roles", async () => {
    await assertFails(asOutsider().collection(`projects/${PROJECT_ID_1}/roles`).get());
  });

  test("initial-batch Leader role can be created for a brand-new project", async () => {
    const db = asOutsider();
    await assertSucceeds(
      db.collection("projects/new-proj/roles").doc("r1").set({
        name: "Leader",
        permissions: fullPermissions(),
        isLeader: true
      })
    );
  });

  test("initial-batch role with a mismatched permission shape is denied", async () => {
    const db = asOutsider();
    await assertFails(
      db.collection("projects/new-proj-2/roles").doc("r1").set({
        name: "Leader",
        permissions: { ...fullPermissions(), manageTags: false },
        isLeader: true
      })
    );
  });

  test("member with manageRoles can create a non-Leader role on an existing project", async () => {
    await assertSucceeds(
      asLeader().collection(`projects/${PROJECT_ID_1}/roles`).doc("new-role").set({
        name: "New Role",
        permissions: noPermissions(),
        isLeader: false
      })
    );
  });

  test("member without manageRoles cannot create a role", async () => {
    await assertFails(
      asMember().collection(`projects/${PROJECT_ID_1}/roles`).doc("new-role").set({
        name: "New Role",
        permissions: noPermissions(),
        isLeader: false
      })
    );
  });

  test("cannot create a second Leader role on an existing project even with manageRoles", async () => {
    await assertFails(
      asLeader().collection(`projects/${PROJECT_ID_1}/roles`).doc("second-leader").set({
        name: "Leader 2",
        permissions: fullPermissions(),
        isLeader: true
      })
    );
  });

  test("member with manageRoles can update a non-Leader role", async () => {
    await assertSucceeds(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/roles`)
        .doc(DEFAULT_ROLE_ID)
        .update({ name: "Renamed Default", permissions: noPermissions() })
    );
  });

  test("the Leader role can never be updated", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/roles`)
        .doc(LEADER_ROLE_ID)
        .update({ name: "Not Leader Anymore" })
    );
  });

  test("a role cannot be promoted to isLeader via update", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/roles`)
        .doc(DEFAULT_ROLE_ID)
        .update({ isLeader: true })
    );
  });

  test("member with manageRoles can delete a non-Leader role", async () => {
    await assertSucceeds(
      asLeader().collection(`projects/${PROJECT_ID_1}/roles`).doc(DEFAULT_ROLE_ID).delete()
    );
  });

  test("the Leader role can never be deleted", async () => {
    await assertFails(
      asLeader().collection(`projects/${PROJECT_ID_1}/roles`).doc(LEADER_ROLE_ID).delete()
    );
  });

  test("succession's Leader can copy a custom role (non-bootstrap shape) into a new project", async () => {
    await assertSucceeds(
      asLeader().collection("projects/new-cycle/roles").doc("r-manager").set({
        name: "Manager",
        permissions: managerPermissions(),
        isLeader: false,
        predecessorProjectId: PROJECT_ID_1
      })
    );
  });

  test("succession role copy is denied for someone who isn't the predecessor project's Leader", async () => {
    await assertFails(
      asMember().collection("projects/new-cycle-2/roles").doc("r-manager").set({
        name: "Manager",
        permissions: managerPermissions(),
        isLeader: false,
        predecessorProjectId: PROJECT_ID_1
      })
    );
  });

  test("succession role copy is denied when claiming leadership of a project the caller isn't Leader of", async () => {
    await assertFails(
      asOutsider().collection("projects/new-cycle-3/roles").doc("r-manager").set({
        name: "Manager",
        permissions: managerPermissions(),
        isLeader: false,
        predecessorProjectId: PROJECT_ID_1
      })
    );
  });
});

describe("projects/{projectId}/members/{userId}", () => {
  beforeEach(seedProjectOne);

  test("a member can list the roster", async () => {
    await assertSucceeds(asMember().collection(`projects/${PROJECT_ID_1}/members`).get());
  });

  test("a non-member, non-self user cannot read another user's membership doc", async () => {
    await assertFails(
      asOutsider().collection(`projects/${PROJECT_ID_1}/members`).doc(LEADER_UID).get()
    );
  });

  test("creator can self-assign the Leader role on a brand-new project", async () => {
    await assertSucceeds(
      asOutsider()
        .collection("projects/new-proj-3/members")
        .doc(OUTSIDER_UID)
        .set({
          userId: OUTSIDER_UID,
          roleId: "r1",
          roleName: "Leader",
          permissions: fullPermissions(),
          displayName: "New Owner",
          photoUrl: null,
          joinedAt: Date.now(),
          isLeader: true
        })
    );
  });

  test("cannot self-assign Leader on an already-existing project", async () => {
    await assertFails(
      asOutsider()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(OUTSIDER_UID)
        .set({
          userId: OUTSIDER_UID,
          roleId: LEADER_ROLE_ID,
          roleName: "Leader",
          permissions: fullPermissions(),
          displayName: "Intruder",
          photoUrl: null,
          joinedAt: Date.now()
        })
    );
  });

  test("joining via a valid role matches the real role doc", async () => {
    await assertSucceeds(
      asOutsider()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(OUTSIDER_UID)
        .set({
          userId: OUTSIDER_UID,
          roleId: DEFAULT_ROLE_ID,
          roleName: "Default",
          permissions: noPermissions(),
          displayName: "Joiner",
          photoUrl: null,
          joinedAt: Date.now(),
          isLeader: false
        })
    );
  });

  test("joining with a spoofed roleName/permissions not matching the role doc is denied", async () => {
    await assertFails(
      asOutsider()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(OUTSIDER_UID)
        .set({
          userId: OUTSIDER_UID,
          roleId: DEFAULT_ROLE_ID,
          roleName: "Default",
          permissions: fullPermissions(),
          displayName: "Joiner",
          photoUrl: null,
          joinedAt: Date.now()
        })
    );
  });

  test("joining with a spoofed isLeader=true is denied", async () => {
    await assertFails(
      asOutsider()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(OUTSIDER_UID)
        .set({
          userId: OUTSIDER_UID,
          roleId: DEFAULT_ROLE_ID,
          roleName: "Default",
          permissions: noPermissions(),
          displayName: "Joiner",
          photoUrl: null,
          joinedAt: Date.now(),
          isLeader: true
        })
    );
  });

  test("joining with a non-existent roleId is denied", async () => {
    await assertFails(
      asOutsider()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(OUTSIDER_UID)
        .set({
          userId: OUTSIDER_UID,
          roleId: "no-such-role",
          roleName: "Default",
          permissions: noPermissions(),
          displayName: "Joiner",
          photoUrl: null,
          joinedAt: Date.now()
        })
    );
  });

  test("cannot write someone else's membership doc", async () => {
    await assertFails(
      asOutsider()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(MEMBER_UID)
        .set({
          userId: MEMBER_UID,
          roleId: DEFAULT_ROLE_ID,
          roleName: "Default",
          permissions: noPermissions(),
          displayName: "Hijack",
          photoUrl: null,
          joinedAt: Date.now()
        })
    );
  });

  test("member with manageRoles can reassign another member's role", async () => {
    await assertSucceeds(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(MEMBER_UID)
        .update({ roleId: "role-manager", roleName: "Manager", permissions: managerPermissions(), isLeader: false })
    );
  });

  test("reassigning with a spoofed isLeader=true is denied", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(MEMBER_UID)
        .update({ roleId: "role-manager", roleName: "Manager", permissions: managerPermissions(), isLeader: true })
    );
  });

  test("member without manageRoles cannot reassign a role", async () => {
    await assertFails(
      asMember()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(MEMBER_UID)
        .update({ roleId: "role-manager", roleName: "Manager", permissions: managerPermissions() })
    );
  });

  test("the Leader's own membership can never be reassigned", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(LEADER_UID)
        .update({ roleId: DEFAULT_ROLE_ID, roleName: "Default", permissions: noPermissions() })
    );
  });

  test("cannot reassign someone to the Leader role via update", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(MEMBER_UID)
        .update({ roleId: LEADER_ROLE_ID, roleName: "Leader", permissions: fullPermissions() })
    );
  });

  test("reassign cannot change the member's userId", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/members`)
        .doc(MEMBER_UID)
        .update({
          userId: OUTSIDER_UID,
          roleId: "role-manager",
          roleName: "Manager",
          permissions: managerPermissions()
        })
    );
  });

  test("member with removeMembers can remove another (non-Leader) member", async () => {
    await assertSucceeds(
      asLeader().collection(`projects/${PROJECT_ID_1}/members`).doc(MEMBER_UID).delete()
    );
  });

  test("member without removeMembers cannot remove another member", async () => {
    await assertFails(
      asMember().collection(`projects/${PROJECT_ID_1}/members`).doc(LEADER_UID).delete()
    );
  });

  test("the Leader can never be removed, even by someone with removeMembers", async () => {
    await assertFails(
      asLeader().collection(`projects/${PROJECT_ID_1}/members`).doc(LEADER_UID).delete()
    );
  });

  test("succession's Leader can write another member's copied membership doc on the new project", async () => {
    await assertSucceeds(
      asLeader()
        .collection("projects/new-cycle/members")
        .doc(MEMBER_UID)
        .set({
          userId: MEMBER_UID,
          roleId: "r-default",
          roleName: "Default",
          permissions: noPermissions(),
          displayName: "Regular Member",
          photoUrl: null,
          joinedAt: Date.now(),
          isLeader: false,
          predecessorProjectId: PROJECT_ID_1
        })
    );
  });

  test("succession member copy is denied for someone who isn't the predecessor project's Leader", async () => {
    await assertFails(
      asMember()
        .collection("projects/new-cycle-2/members")
        .doc(OUTSIDER_UID)
        .set({
          userId: OUTSIDER_UID,
          roleId: "r-default",
          roleName: "Default",
          permissions: noPermissions(),
          displayName: "Outsider",
          photoUrl: null,
          joinedAt: Date.now(),
          isLeader: false,
          predecessorProjectId: PROJECT_ID_1
        })
    );
  });

  test("succession member copy is denied when claiming leadership of a project the caller isn't Leader of", async () => {
    await assertFails(
      asOutsider()
        .collection("projects/new-cycle-3/members")
        .doc(MEMBER_UID)
        .set({
          userId: MEMBER_UID,
          roleId: "r-default",
          roleName: "Default",
          permissions: noPermissions(),
          displayName: "Regular Member",
          photoUrl: null,
          joinedAt: Date.now(),
          isLeader: false,
          predecessorProjectId: PROJECT_ID_1
        })
    );
  });
});

describe("collectionGroup('members') query (observeUserProjects)", () => {
  beforeEach(seedProjectOne);

  test("a user can list their own membership docs across all projects", async () => {
    await assertSucceeds(
      asMember()
        .collectionGroup("members")
        .where("userId", "==", MEMBER_UID)
        .get()
    );
  });

  test("a user cannot list another user's membership docs via the collectionGroup query", async () => {
    await assertFails(
      asOutsider()
        .collectionGroup("members")
        .where("userId", "==", MEMBER_UID)
        .get()
    );
  });
});

describe("inviteCodes/{code}", () => {
  const CODE = "ABC12345";

  beforeEach(async () => {
    await seedProjectOne();
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context
        .firestore()
        .collection("inviteCodes")
        .doc(CODE)
        .set({ projectId: PROJECT_ID_1, expiresAt: Date.now() + 1000000, isActive: true });
    });
  });

  test("any signed-in user can get an invite code by its exact id (join flow, pre-membership)", async () => {
    await assertSucceeds(asOutsider().collection("inviteCodes").doc(CODE).get());
  });

  test("unauthenticated cannot get an invite code", async () => {
    await assertFails(asUnauthenticated().collection("inviteCodes").doc(CODE).get());
  });

  test("known accepted gap: a non-member CAN list invite codes for a project they don't belong to", async () => {
    await assertSucceeds(
      asOutsider().collection("inviteCodes").where("projectId", "==", PROJECT_ID_1).get()
    );
  });

  test("member with manageInviteCode can create an invite code", async () => {
    await assertSucceeds(
      asLeader()
        .collection("inviteCodes")
        .doc("NEWCODE1")
        .set({ projectId: PROJECT_ID_1, expiresAt: Date.now() + 1000000, isActive: true })
    );
  });

  test("member without manageInviteCode cannot create an invite code", async () => {
    await assertFails(
      asMember()
        .collection("inviteCodes")
        .doc("NEWCODE2")
        .set({ projectId: PROJECT_ID_1, expiresAt: Date.now() + 1000000, isActive: true })
    );
  });

  test("member with manageInviteCode can update (deactivate) an invite code", async () => {
    await assertSucceeds(
      asLeader().collection("inviteCodes").doc(CODE).update({ isActive: false })
    );
  });

  test("invite code delete is always denied, even with manageInviteCode", async () => {
    await assertFails(asLeader().collection("inviteCodes").doc(CODE).delete());
  });
});

const TASK_ID = "task-1";

async function seedTask(overrides = {}) {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection(`projects/${PROJECT_ID_1}/tasks`)
      .doc(TASK_ID)
      .set({
        title: "Print vendor quotes",
        description: null,
        holderUid: LEADER_UID,
        holderDisplayName: "Leader Person",
        status: "TODO",
        dueDate: null,
        timesHandedOver: 0,
        createdByUid: LEADER_UID,
        createdByDisplayName: "Leader Person",
        createdAt: Date.now(),
        updatedAt: Date.now(),
        ...overrides
      });
  });
}

describe("projects/{projectId}/tasks/{taskId}", () => {
  beforeEach(async () => {
    await seedProjectOne();
    await seedTask();
  });

  test("member can read a task", async () => {
    await assertSucceeds(asMember().collection(`projects/${PROJECT_ID_1}/tasks`).doc(TASK_ID).get());
  });

  test("non-member cannot read a task", async () => {
    await assertFails(asOutsider().collection(`projects/${PROJECT_ID_1}/tasks`).doc(TASK_ID).get());
  });

  test("member with assignTasks can create a task, becoming its holder", async () => {
    await assertSucceeds(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/tasks`)
        .doc("task-new")
        .set({
          title: "New task",
          description: null,
          holderUid: LEADER_UID,
          holderDisplayName: "Leader Person",
          status: "TODO",
          dueDate: null,
          timesHandedOver: 0,
          createdByUid: LEADER_UID,
          createdByDisplayName: "Leader Person",
          createdAt: Date.now(),
          updatedAt: Date.now()
        })
    );
  });

  test("member without assignTasks cannot create a task", async () => {
    await assertFails(
      asMember()
        .collection(`projects/${PROJECT_ID_1}/tasks`)
        .doc("task-new-2")
        .set({
          title: "New task",
          description: null,
          holderUid: MEMBER_UID,
          holderDisplayName: "Regular Member",
          status: "TODO",
          dueDate: null,
          timesHandedOver: 0,
          createdByUid: MEMBER_UID,
          createdByDisplayName: "Regular Member",
          createdAt: Date.now(),
          updatedAt: Date.now()
        })
    );
  });

  test("cannot create a task claiming someone else as its holder", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/tasks`)
        .doc("task-new-3")
        .set({
          title: "New task",
          description: null,
          holderUid: MEMBER_UID,
          holderDisplayName: "Regular Member",
          status: "TODO",
          dueDate: null,
          timesHandedOver: 0,
          createdByUid: LEADER_UID,
          createdByDisplayName: "Leader Person",
          createdAt: Date.now(),
          updatedAt: Date.now()
        })
    );
  });

  test("current holder can mark their own task done", async () => {
    await assertSucceeds(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/tasks`)
        .doc(TASK_ID)
        .update({ status: "DONE", updatedAt: Date.now() })
    );
  });

  test("non-holder cannot mark a task done", async () => {
    await assertFails(
      asMember()
        .collection(`projects/${PROJECT_ID_1}/tasks`)
        .doc(TASK_ID)
        .update({ status: "DONE", updatedAt: Date.now() })
    );
  });

  test("member without editAnyTask cannot delete a task, even one they hold", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.firestore().collection(`projects/${PROJECT_ID_1}/tasks`).doc(TASK_ID).update({
        holderUid: MEMBER_UID,
        holderDisplayName: "Regular Member"
      });
    });
    await assertFails(asMember().collection(`projects/${PROJECT_ID_1}/tasks`).doc(TASK_ID).delete());
  });

  test("member with editAnyTask can delete a task they don't hold", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.firestore().collection(`projects/${PROJECT_ID_1}/members`).doc(MEMBER_UID).update({
        permissions: { ...noPermissions(), editAnyTask: true }
      });
    });
    await assertSucceeds(asMember().collection(`projects/${PROJECT_ID_1}/tasks`).doc(TASK_ID).delete());
  });
});

const HANDOFF_ID = "handoff-1";

describe("projects/{projectId}/tasks/{taskId}/handoffs/{handoffId}", () => {
  beforeEach(async () => {
    await seedProjectOne();
    await seedTask();
  });

  test("current holder can offer a handoff", async () => {
    await assertSucceeds(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/tasks/${TASK_ID}/handoffs`)
        .doc(HANDOFF_ID)
        .set({
          projectId: PROJECT_ID_1,
          fromUid: LEADER_UID,
          fromDisplayName: "Leader Person",
          toUid: MEMBER_UID,
          toDisplayName: "Regular Member",
          note: null,
          status: "OFFERED",
          declineReason: null,
          offeredAt: Date.now(),
          respondedAt: null
        })
    );
  });

  test("non-holder cannot offer a handoff", async () => {
    await assertFails(
      asMember()
        .collection(`projects/${PROJECT_ID_1}/tasks/${TASK_ID}/handoffs`)
        .doc(HANDOFF_ID)
        .set({
          projectId: PROJECT_ID_1,
          fromUid: MEMBER_UID,
          fromDisplayName: "Regular Member",
          toUid: LEADER_UID,
          toDisplayName: "Leader Person",
          note: null,
          status: "OFFERED",
          declineReason: null,
          offeredAt: Date.now(),
          respondedAt: null
        })
    );
  });

  async function seedHandoff() {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context
        .firestore()
        .collection(`projects/${PROJECT_ID_1}/tasks/${TASK_ID}/handoffs`)
        .doc(HANDOFF_ID)
        .set({
          projectId: PROJECT_ID_1,
          fromUid: LEADER_UID,
          fromDisplayName: "Leader Person",
          toUid: MEMBER_UID,
          toDisplayName: "Regular Member",
          note: null,
          status: "OFFERED",
          declineReason: null,
          offeredAt: Date.now(),
          respondedAt: null
        });
    });
  }

  test("offered toUid can accept the handoff", async () => {
    await seedHandoff();
    await assertSucceeds(
      asMember()
        .collection(`projects/${PROJECT_ID_1}/tasks/${TASK_ID}/handoffs`)
        .doc(HANDOFF_ID)
        .update({ status: "ACCEPTED", respondedAt: Date.now() })
    );
  });

  test("someone other than the offered toUid cannot accept the handoff", async () => {
    await seedHandoff();
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/tasks/${TASK_ID}/handoffs`)
        .doc(HANDOFF_ID)
        .update({ status: "ACCEPTED", respondedAt: Date.now() })
    );
  });

  test("handoff delete is always denied", async () => {
    await seedHandoff();
    await assertFails(
      asMember().collection(`projects/${PROJECT_ID_1}/tasks/${TASK_ID}/handoffs`).doc(HANDOFF_ID).delete()
    );
  });
});

describe("projects/{projectId}/events/{eventId}", () => {
  beforeEach(seedProjectOne);

  test("member can create an event record attributed to themselves", async () => {
    await assertSucceeds(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/events`)
        .doc("event-1")
        .set({
          type: "TASK_DELETED",
          taskId: TASK_ID,
          taskTitle: "Print vendor quotes",
          byUid: LEADER_UID,
          byDisplayName: "Leader Person",
          at: Date.now()
        })
    );
  });

  test("cannot create an event record attributed to someone else", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/events`)
        .doc("event-2")
        .set({
          type: "TASK_DELETED",
          taskId: TASK_ID,
          taskTitle: "Print vendor quotes",
          byUid: MEMBER_UID,
          byDisplayName: "Regular Member",
          at: Date.now()
        })
    );
  });

  test("a member can read the project's events (Pulse tab)", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.firestore().collection(`projects/${PROJECT_ID_1}/events`).doc("event-3").set({
        type: "TASK_DELETED",
        taskId: TASK_ID,
        taskTitle: "Print vendor quotes",
        byUid: LEADER_UID,
        byDisplayName: "Leader Person",
        at: Date.now()
      });
    });
    await assertSucceeds(asLeader().collection(`projects/${PROJECT_ID_1}/events`).doc("event-3").get());
  });

  test("a non-member cannot read the project's events", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.firestore().collection(`projects/${PROJECT_ID_1}/events`).doc("event-4").set({
        type: "TASK_DELETED",
        taskId: TASK_ID,
        taskTitle: "Print vendor quotes",
        byUid: LEADER_UID,
        byDisplayName: "Leader Person",
        at: Date.now()
      });
    });
    await assertFails(asOutsider().collection(`projects/${PROJECT_ID_1}/events`).doc("event-4").get());
  });

  test("creating an event with an unrecognized type is denied", async () => {
    await assertFails(
      asLeader()
        .collection(`projects/${PROJECT_ID_1}/events`)
        .doc("event-5")
        .set({
          type: "SOMETHING_MADE_UP",
          taskId: TASK_ID,
          taskTitle: "Print vendor quotes",
          byUid: LEADER_UID,
          byDisplayName: "Leader Person",
          at: Date.now()
        })
    );
  });
});
