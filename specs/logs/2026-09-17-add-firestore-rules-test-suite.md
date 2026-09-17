# 2026-09-17 (session 8) — Firestore emulator + Jest rules test suite

## Context
Continuation of Phase 1 completion. `firestore.rules` had never been tested against the Firestore
emulator — only manually verified against production across several earlier sessions. This builds
the test suite from scratch (no `package.json`, no `.firebaserc`, no existing tests found).

## Fix
New files:
- `package.json` — devDependencies `jest`, `@firebase/rules-unit-testing`; `npm run test:rules` runs
  `firebase emulators:exec --only firestore --project demo-temacker "jest"`.
- `.firebaserc` — `default: demo-temacker` (a `demo-`-prefixed project id per Firebase's
  emulator-testing convention; no real project needed, no risk to the real `temacker-a0252` project).
- `firebase.json` — added an `emulators.firestore.port` block (8080) alongside the existing rules/
  indexes config.
- `.gitignore` — added `node_modules/`, `firebase-debug.log`, `firestore-debug.log`, `ui-debug.log`.
- `firestore-tests/rules.test.js` — 45 test cases across every collection (`projects`, `roles`,
  `members`, the `members` collectionGroup query, `inviteCodes`): non-member/unauthenticated reads
  denied, exact permission-gated writes, Leader role/membership immutability, the invite-code list
  loosening asserted as an intentional accepted gap (not silently tightened later). Fixture field
  shapes match `ProjectMapper`/`RoleMapper`/`RolePermissionsMapper`/`MembershipMapper`/
  `InviteCodeMapper` exactly.

## Real bug found and fixed by the test suite
One test failed on the first run: "cannot self-assign Leader on an already-existing project"
(expected failure, got success). The `members` create rule's "joining an existing project" branch
validated that incoming `roleName`/`permissions` matched the referenced role doc, but never excluded
`isLeader == true` — unlike the update (reassign-role) path, which already has that guard
(`firestore.rules`, member update rule). This meant anyone who knew a project's Leader role
document ID (an opaque Firestore auto-ID — not secret by design, but not cryptographically
unguessable either, e.g. if ever exposed by a future invite/share flow) could write a membership
doc claiming the Leader role on someone else's existing project, since the well-known Leader field
values (`roleName: "Leader"`, `permissions: fullPermissions()`) are not secret. Fixed by adding the
same `roleDoc(...).isLeader == false` guard already present on the update path. All 45 tests pass
after the fix. **Deployed to production** (`firebase deploy --only firestore:rules --project
temacker-a0252`), per explicit user confirmation.

## Side-findings (flagged, not fixed — out of scope)
- `ProjectMapper` never writes a `defaultRoleId` field despite the rules file's header comment
  describing one on the `projects` doc — worth checking where (if anywhere) that's actually set.
- The `deleteProject` permission flag is stored on every role but never enforced by any rule
  (`projects` update/delete is hard-denied for everyone regardless of permission) — effectively dead,
  consistent with item #7's finding that no delete-project feature exists yet.
- `observeUserProjects()`'s remote-sync branch only upserts `projectDao`, never `membershipDao` — a
  user added to or removed from a project by someone else won't see their own project list update
  until they separately visit that project (related to, but distinct from, the stale-UI fixes in
  the previous two commits).

## Verification
`npm run test:rules` — 45/45 pass.

## Scope
Per CLAUDE.md rule 14, no other rules changes made beyond the one real gap the test suite surfaced.
