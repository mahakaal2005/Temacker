# 2026-09-15 — Firebase CLI wired up, golden path walked end-to-end, three real bugs fixed

## Context
User added a billing account to the Firebase project and asked to check whether Firebase CLI was
set up (parallel to gcloud). From there this turned into deploying the still-undeployed
`firestore.rules` and then actually walking the Phase 1 golden path on the connected physical
device (`adb`), which is exactly what `2026-08-31`'s log left open.

## Done
- Confirmed Firebase CLI already installed (v15.28.2), already logged in, project visible.
- Added `firebase.json` + `firestore.indexes.json` (repo had neither — this is why rules were
  never deployed), ran `firebase use --add temacker-a0252`, deployed `firestore.rules`.
- Built, installed, and launched the app on the connected device (`RZCWA28EAZF`). Drove the UI via
  `adb shell input` + `uiautomator dump` for element bounds, screenshotting at each step.
- User signed in with Google manually (I avoid driving the system Credential Manager picker
  blindly — risk of a blocking system dialog). Walked: Login → No-project (gate correctly found
  zero projects) → Create project → **found bug #1**.
- **Bug #1 — `firestore.rules` map literals**: `fullPermissions()`/`noPermissions()` used bare
  identifier keys (`manageRoles: true`) instead of quoted strings. Firestore's rules language
  parses bare keys as variable references, producing "Invalid variable name" warnings on deploy
  that looked cosmetic but weren't — both functions silently fail to evaluate, so every
  permission check depending on them denies. Fixed by quoting the keys; redeployed with zero
  warnings.
- **Bug #2 — collection-group query denied**: `observeUserProjects()`'s
  `collectionGroup("members").whereEqualTo("userId", uid)` query still got `PERMISSION_DENIED`
  after fixing bug #1. Root cause: Firestore validates `list`/collection-group queries from the
  query's own constraints alone — it does not execute `isMember(projectId)`'s `exists()` check
  per result document across a variable `projectId`. Fixed by adding
  `|| resource.data.userId == request.auth.uid` to the `members` read rule, which Firestore *can*
  verify statically against the query's own filter.
- **Bug #3 — `observeUserProjects()` always empty**: `FirestoreProjectRemoteDataSource` read a
  `projectId` field off each member doc that `MembershipMapper.toFirestoreMap()` never writes
  (it's implicit in the doc's path, `projects/{projectId}/members/{userId}`). Fixed by deriving
  `projectId` from `it.reference.parent.parent?.id` instead.
- **Bug #4 — new project never appears after creation**: even with #3 fixed, the UI-facing
  `observeUserProjects()` emission is driven by local Room (`membershipDao.observeByUser(uid)`),
  not the remote listener. `joinProject()` seeds a `membershipDao` row on success; `createProject()`
  never did the equivalent for the creator's own Leader membership. Changed
  `createProjectWithLeader()` to return `Pair<Project, Membership>` so
  `OfflineFirstProjectRepository.createProject()` can upsert both `projectDao` and `membershipDao`
  immediately, matching the join path.
- Removed the ID-token debug logging scaffolding from `FirestoreProjectRemoteDataSource` (added
  2026-08-31 session to debug what turned out to be an unrelated auth question — token claims
  were always correct; these four bugs were rules/data-layer, not auth).
- Verified the full golden path after all four fixes, fresh install: Login → Google Sign-In →
  No-project (correct — new account) → Create project → Home now correctly shows
  "1 members · You lead" → Roster now correctly shows the Leader row, "1 members", Manage roles
  link, invite button.
- `./gradlew assembleDebug` and `./gradlew lintDebug` both pass clean.
- Cleaned up: deleted the two test projects (`GoldenPathTest`, `GoldenPathTest2`) from
  `temacker-a0252` via Firestore REST + `firebase firestore:delete --recursive` (user confirmed
  first — this is a destructive production action).
- Committed as two commits: `firebase.json`/`firestore.indexes.json` add, then the four-bug fix.

## Still open
- Roster → Manage roles, invite code generation/join flow, remove member, reassign role — none
  of these were exercised this session. Worth a follow-up pass now that the base golden path
  works.
- ViewModel unit tests still not started (per `progress.md`).
- Firestore rules still not tested against the emulator, only against production via manual
  click-through — fine for now but worth automating before Phase 2.
- Sign-out and re-entry (does the gate correctly route back to Home for an existing member,
  not No-project?) not explicitly re-verified after these fixes, though the underlying data path
  is the same one just fixed.

## Next
- Exercise invite-code join flow with a second account/device to validate the `inviteCodes` rules
  and the join path end-to-end.
- Start ViewModel unit tests per phase-1 spec's Testing section.
