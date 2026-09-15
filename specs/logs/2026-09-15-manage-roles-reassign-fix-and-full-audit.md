# 2026-09-15 (session 3) — Manage Roles/Remove Member/Reassign Role verified, reassignRole bug fixed, full 4-module code audit

## Context
Continuation of today's earlier sessions. Per the approved plan, exercised the remaining
untested Phase 1 screens (Manage Roles, Remove Member, Reassign Role) on-device using a synthetic
non-Leader member (`testMemberFake001`, Editor role) injected via Firestore REST, since no second
Google account/device is available. Partway through, found a real bug and a systemic
"stale Compose UI after mutation" symptom, then pivoted (per user request) from manual on-device
debugging to a full feature-by-feature code review, dispatched as 4 parallel subagents (one per
module) checked against the project's `.claude/skills/` and `CLAUDE.md`.

## Done — Manage Roles / Remove Member / Reassign Role on-device verification
- **Manage Roles**: create role ("Scheduler", "TestRepro" — both cleaned up via the app's own
  Delete button), toggle a permission (Editor role's `manageInviteCode`), delete a role — all
  confirmed correct via Firestore REST ground truth. Leader role card confirmed locked/non-editable.
- **Reassign Role**: exercised via the synthetic "Test Member" (Editor) roster row's "⋮" → "Reassign
  role" → picked "Default", confirmed via Firestore REST that the member doc's `roleId`/`roleName`/
  `permissions` updated correctly. **Found and fixed a real bug in the process** (below).
- **Remove Member**: not yet exercised this session — deferred to next session (see Next).
- Repeatedly hit a **stale-UI-after-mutation** symptom this session: after deleting a role or
  reassigning a member's role, the on-device screen did not visually update (list still showed the
  old/deleted item) even after the underlying Firestore write was confirmed successful and, in some
  cases, even after Room itself was confirmed updated. In every case, a full app restart (force-stop
  + relaunch) immediately showed the correct state. The presentation-layer review agent (see below)
  found no cause in the Compose/ViewModel code itself (correct `key = { it.id }`, correct
  `collectAsStateWithLifecycle()`, no `remember`-cached snapshots) — likely a data-layer/offline-first
  Flow-emission timing issue rather than a Compose bug. Not root-caused this session; flagged as a
  priority for next session (see Next).

## Bug fixed: `OfflineFirstMembershipRepository.reassignRole()` never updated Room locally
- **Root cause**: `MembershipRemoteDataSource.reassignRole()` returned `EmptyResult<DataError>` (no
  data), so `OfflineFirstMembershipRepository.reassignRole()` had no updated `Membership` to write
  into `membershipDao` — unlike every sibling mutation (`removeMember`, `createRole`, `updateRole`,
  `deleteRole`, `joinProject`), which all follow the offline-first pattern of `remote call ->
  onSuccess { dao upsert/delete }`. The comment on the old code claimed "Room row refreshes from the
  live listener in observeMembers/observeMembership" — this session's on-device testing showed that
  claim doesn't hold reliably: with instrumented logging, the Room upsert (when added) sometimes
  took **up to ~56 seconds** to actually land on disk even though the write itself succeeded near-
  instantly and no exception was thrown — meaning the live listener alone is not a trustworthy way to
  keep Room current, and every mutation needs its own explicit local write (matching the codebase's
  existing convention everywhere else).
- **Fix**: `FirestoreMembershipRemoteDataSource.reassignRole()` now returns `Result<Membership,
  DataError>` (re-reads the member doc after the update and maps it, mirroring `createRole`'s
  pattern), and `OfflineFirstMembershipRepository.reassignRole()` now does
  `.onSuccess { membershipDao.upsertAll(listOf(it.toEntity())) }.asEmptyResult()`. Also updated the
  `MembershipRemoteDataSource` interface signature to match.
- `./gradlew assembleDebug` and `lintDebug` both pass clean after the fix.
- **Not fully explained**: even after this fix, the on-device Roster screen still didn't visually
  reflect the reassignment until app restart in one test run, despite Room's confirmed-correct data
  eventually being present (after the ~56s delay noted above). This suggests there may be a second,
  separate cause — possibly Room/SQLite single-writer contention under load, or something about the
  physical device's background app activity — worth investigating with fresh eyes next session rather
  than continuing to debug reactively on-device (this is what triggered the pivot to a static code
  review instead, per user's explicit direction to stop "burning tokens debugging" on a small app and
  review the code systematically instead).

## Full code review (4 parallel subagents, one per module, checked against `.claude/skills/*` + `CLAUDE.md`)

### Real bugs found (not yet fixed — logged for next session)
1. **`OfflineFirstRoleRepository.updateRole()`** (`feature_project/data/repository/`) hardcodes
   `isLeader = false` when writing the post-update role into Room, instead of preserving the role's
   real `isLeader` value — same root-cause class as the `reassignRole` bug just fixed:
   `RoleRemoteDataSource.updateRole()` returns `EmptyResult` instead of the updated `Role`, so the
   repository fabricates a local entity rather than using real data. If ever called against the
   Leader role, this would silently flip Room's `isLeader` flag to `false` while Firestore stays
   correct. Fix direction: change `updateRole`'s return type to `Result<Role, DataError>` (mirror
   `createRole` in the same interface), upsert the real returned role.
2. **Google sign-in cancel shows a spurious error.** `FirebaseAuthRemoteDataSource`'s
   `GetCredentialCancellationException` catch block's own comment says "user dismissed the picker —
   not an error, just stop," but the code still returns `Result.Error(DataError.Network.UNKNOWN)`,
   which `LoginViewModel`/`LoginScreen` then renders as a visible error to the user for a simple
   cancel.
3. **All auth failures collapse to one generic error.** Every failure path in
   `signInWithGoogle`/`signInWithEmail`/`registerWithEmail` (wrong password, weak/duplicate email, no
   internet, malformed token) maps to the same `DataError.Network.UNKNOWN`, even though more specific
   `DataError` cases already exist and go unused — wrong-password and offline look identical to the
   user.
4. **Firestore exceptions and listener errors are swallowed with zero logging.**
   `SafeFirestoreCall.kt`'s generic `catch (e: Exception)` branch and both `FirestoreFlow.kt`
   `snapshots()` extensions' `close(error)` calls have no `Log.e`/stack trace at all — this is the
   exact "silent, no error UI, no log line" pattern a prior session already flagged as a real problem
   for invite-code generation; it's still the norm everywhere in the data layer, not just that one
   call site.

### Inconsistencies (not crashing, but real UX/architecture gaps)
5. **Roster/ManageRoles mutations swallow errors.** `RosterViewModel`'s `removeMember`,
   `reassignMemberRole`, `generateCode`, and `ManageRolesViewModel`'s `createRole`, `updateRole`,
   `deleteRole` are all `viewModelScope.launch { useCase(...) }` with no `.onFailure`/Event/snackbar —
   unlike `CreateProjectViewModel`/`JoinProjectViewModel`, which correctly chain
   `.onSuccess{}.onFailure{ error -> update state }`. If any of these Firestore calls fail (e.g. a
   rules permission error), the UI silently no-ops with no feedback.
6. **Profile screen has no error-surfacing path at all** — `ProfileState` has no `error: UiText?`
   field and `ProfileEvent` has no error/snackbar case, so failures from `observeUserProjects`/
   `observeCurrentMembership` have no way to reach the UI.
7. **Missing `@Index("projectId")`** on `RoleEntity`/`MembershipEntity`/`InviteCodeEntity`, all
   queried by `projectId` in their DAOs — standard Room/SQLite practice, normally a Room-compiler
   warning. Also **no cascade-delete**: deleting a project leaves orphaned roles/memberships/invite
   codes in Room with no bulk-delete-by-projectId DAO method on any of the three child tables.
8. **`DataError` enum diverges from the `android-error-handling` skill's canonical set** (missing
   `BAD_REQUEST`/`FORBIDDEN`/`NOT_FOUND`/`TOO_MANY_REQUESTS`/`PAYLOAD_TOO_LARGE`/
   `SERVICE_UNAVAILABLE`; adds Firestore-specific `PERMISSION_DENIED`) without this deviation ever
   being flagged/approved per CLAUDE.md rule 2. Plausibly a deliberate, reasonable Firestore-vs-REST
   adaptation, but undocumented. Also `DataErrorToUiText.kt` has no explicit branch for
   `REQUEST_TIMEOUT` (silently falls to a generic "unknown" message).
9. **Missing/partial `@Preview` coverage**: `ProjectGateScreen` has none at all (and also has no
   Root/Screen split — the UI is inlined directly in `ProjectGateRoot`); `RosterScreen` covers only 1
   of ~5 real UI states (`isLoading`, `isInviteSheetVisible`, `reassignTargetUserId` dialog all
   un-previewed); `ManageRolesScreen` is missing loading/empty-role-list states; `HomeScreen` is
   missing an empty-state preview.

### Minor / style
10. Password-visibility toggle in `LoginScreen.kt` lives in a local `remember { mutableStateOf }`
    instead of `LoginState`/`LoginAction`, against `android-compose-ui`'s state-ownership rule (its
    only exception list is framework-owned state like `LazyListState`).
11. `LoginState` contains an unstable `UiText` (sealed interface) field with no `@Stable` annotation
    per the compose-ui skill's stability rules.
12. Unused `Stone` design-system color token (`core/presentation/designsystem/Color.kt`); two
    comment blocks (`CoreModule.kt`, `CurrentActivityHolder.kt`) exceed CLAUDE.md's strict one-line
    comment rule.

### Checked and confirmed clean (no findings)
- Offline-first pattern everywhere else (`OfflineFirstProjectRepository`, post-fix
  `OfflineFirstMembershipRepository`, `OfflineFirstInviteCodeRepository`).
- All Room/Firestore mappers — every domain field round-trips correctly, no silent drops.
- All 14 use cases — thin one-line pass-throughs, no leaked logic.
- `ProjectModule.kt`/`AuthModule.kt`/`CoreModule.kt` Koin wiring — correct registration and scoping
  throughout, no duplicates.
- Navigation — type-safe routes, no cross-feature imports anywhere, callback-based cross-feature nav
  used correctly (including Profile's `onNavigateToProfile`/`onNavigateToLogin`).
- `CurrentActivityHolder` — `WeakReference<Activity>`, cleared on `onActivityPaused`, no leak risk, no
  `GlobalScope` usage anywhere in the codebase.
- Comment style — clean everywhere except the two files noted in #12.
- `App.kt`/`MainActivity.kt` — all 4 Koin modules registered, `NavHost` wiring and
  `popUpTo(0){inclusive=true}` sign-out/login transitions correct, no dead/placeholder code.

## Still open / not yet cleaned up
- Synthetic test data still in Firestore (`temacker-a0252`, project `VerifyFix` /
  `FCLLqo2Jx2Apdql2ijLL`): role `testRoleEditor` (Editor) and member `testMemberFake001` (Test
  Member). Needs cleanup via Firestore REST once Remove Member is also tested against it (removing
  the member is itself the next test to run, which will clean up the member doc as a side effect;
  the role doc will still need manual cleanup after).
- Remove Member action not yet exercised on-device.
- The ~56-second Room-write-delay / stale-UI-until-restart symptom is not root-caused. Worth
  investigating with fresh eyes (possibly Room single-writer contention, possibly something specific
  to this physical device) before trusting any "instant" UI-update assumption on mutation screens.
- ViewModel unit tests, Firestore emulator rules test suite (Part B of today's earlier plan), and
  invite-code join-side testing (needs a second account) all still not started.

## Next (per user's decision — findings logged now, fixes to be planned and done one-by-one in future sessions)
1. Fix the 4 real bugs (role update `isLeader` corruption, sign-in-cancel false error, collapsed auth
   errors, silent Firestore exception swallowing) — one at a time, each verified with a build/lint
   pass and (where feasible) an on-device check.
2. Address the 4 inconsistencies (error-surfacing gaps in Roster/ManageRoles/Profile, Room indices +
   cascade-delete, `DataError` alignment).
3. Clean up minor/style items and missing previews.
4. Investigate the stale-UI-after-mutation / delayed-Room-write symptom properly before further
   feature work, since it undermines confidence in "the write succeeded" as a proxy for "the user
   sees the correct state."
5. Clean up remaining synthetic Firestore test data once Remove Member is tested.
6. Resume Part B (Firestore emulator + Jest rules test suite) from the earlier approved plan.
