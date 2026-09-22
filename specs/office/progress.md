# Progress

Read this first, every session, before touching code — it's the current status snapshot. Full task
detail lives in each phase's spec file (`specs/office/phase-N-*.md`); this file only tracks
done/left. Update it after every session or completed task.

**Current phase: 5b — project switcher, code complete and partially device-verified (2026-09-22, see phase-5b-project-switcher.md); the actual multi-project tap-through is the one piece still unverified. Phase 6 next after that's closed out. Earlier: 5 done (2026-09-22). 4 — Truth about the team (implemented and verified on-device, 2026-09-19; unit tests still deferred). Phase 3 complete (2026-09-19): notifications + offline outbox built and verified on-device, see phase-3-notifications-offline.md (Phase 4 was pulled forward per explicit user request; see phase-4-team-truth.md's note).**

## Phase 1 — Auth, projects, roles (`phase-1-auth-projects-roles.md`)
- [x] Core layer: `Result`/`DataError` (core/domain/util), `SessionManager` interface + DataStore impl, `UiText`/`ObserveAsEvents` (core/presentation/util), `CoreModule` Koin wiring, `App.kt` + `startKoin`. `AppDatabase` deferred until the first Room entity exists (Room rejects `@Database` with zero entities).
- [x] feature_auth scaffold: domain (`User`, `AuthRepository`, `ObserveSessionUseCase`), Splash + Login screens with previews, `authGraph` nav, `AuthModule` DI, wired into `App.kt`/`MainActivity.kt`.
- [x] Real Firebase Auth wired: `AuthRemoteDataSource` / `FirebaseAuthRemoteDataSource` (Credential Manager Google ID token → `FirebaseAuth.signInWithCredential`, with authorized-accounts-first-then-full-picker fallback for new users), `OfflineFirstAuthRepository` (coordinates remote + `SessionManager`), `InMemoryAuthRepository` deleted. `google_web_client_id` in `strings.xml` holds the real Web client ID now. Use cases: `SignInWithGoogleUseCase`, `SignInWithEmailUseCase`, `RegisterWithEmailUseCase` (replaces the old single `LoginUseCase`). Login screen has email/password fields + sign-in/register toggle alongside Google; registration sends a verification email (fire-and-forget, doesn't block account creation). `CurrentActivityHolder` (core/data/activity, `createdAtStart = true` in Koin) feeds Credential Manager the foreground Activity instead of the Application context Koin injects everywhere else — a real bug that silently broke the picker. Delete `MainActivity.kt`'s `AppPlaceholderScreen`/`AppPlaceholderRoute` once feature_project's real Home screen exists.
  - **Resolved (2026-08-31):** Google Sign-In now works end-to-end on a physical device. Root cause was a wrong SHA-1 fingerprint registered in Firebase (not a code bug) — the "verified correct" note above was wrong; re-verify config claims like this against the actual console next time rather than trusting a prior session's note. Along the way, `signInWithGoogle()` was hardened: the no-authorized-account fallback now uses `GetSignInWithGoogleOption` (button-flow picker) instead of `GetGoogleIdOption(filterByAuthorizedAccounts=false)`, which was itself throwing `NoCredentialException` on some devices even with accounts present; added a SHA-256-hashed nonce (was being passed raw, which did nothing) per Google's Credential Manager guidance; added logging to the `GetCredentialCancellationException` branch, which was previously silent and briefly masked a misleading "[16] Account reauth failed" failure during debugging. Removed `MainActivity.kt`'s `printSha1()` debug scaffolding (added mid-session to diagnose the SHA-1 issue, no longer needed).
- [x] Design system: `core/presentation/designsystem/{Color,Type,Shape,AppTheme}.kt` replaces the stock Android Studio template (`ui/theme/`, deleted). Also fixed `themes.xml`'s XML parent theme, which was `Theme.Material.Light` — wrong for a strictly-dark app and the likely cause of the white flash/washed-out look. Previews now wrap in `TemackerTheme` instead of a bare `MaterialTheme { }` (which defaults to light M3), so IDE previews finally match the real app.
- [x] feature_project scaffold: domain (`Project`, `Role`, `Membership`, `InviteCode`, `RolePermissions`), repositories, 12 use cases (create/join project, observe project/members/roles/current membership/user projects, create/update/delete role, remove member, reassign role, generate/observe invite code). Firestore remote data sources + mappers, Room entities/DAOs (`core/data/database/`), offline-first repositories, `ProjectModule` (Koin).
- [x] Screens: Splash, Login, No-project, Create project, Join project, Home stub, Roster, Manage roles all built (Root/Screen/ViewModel/State/Action/Event, MVI, `@Preview`s). Profile screen (`feature_profile`) built to match `specs/UI/temacker-all-phases-android-concept.html`'s P1·09 layout — avatar, current project/role/member-since card, coral-outlined sign-out.
- [x] Compile-error pass (2026-08-27): the above had landed uncompiled from a prior session that hit the API limit mid-work. Fixed: missing `androidx.compose.runtime.getValue` imports (delegate resolution), missing `@OptIn(ExperimentalMaterial3Api::class)` on 6 screens using `TopAppBar`/`ModalBottomSheet`, `Icons.Default.PersonAdd` unresolved (added `material-icons-extended` dependency to the version catalog + `app/build.gradle.kts`). `./gradlew assembleDebug` passes clean.
- [x] Wired up: `projectModule`/`profileModule` added to `App.kt`'s `startKoin`. Built `feature_project`'s nav graph (`ProjectRoutes.kt`/`ProjectGraph.kt`) and `feature_profile`'s (`ProfileRoutes.kt`/`ProfileGraph.kt`), both using cross-feature callbacks per architecture §4 (never importing another feature's routes). Added `ProjectGateViewModel`/`ProjectGateScreen` — a one-shot decision screen after login that routes to No-project vs Home based on whether `ObserveUserProjectsUseCase` returns any projects (this didn't exist before; every project ViewModel independently picks the user's first project, but nothing decided the *initial* landing screen). `MainActivity.kt`'s `AppPlaceholderRoute`/`AppPlaceholderScreen` deleted, replaced with the real `NavHost` wiring all three graphs (auth → project gate → project screens ↔ profile, with sign-out routing back to Login).
- [x] **On-device golden path verified (2026-09-15):** sign in (Google) → No-project gate → create project → Home ("1 members · You lead") → Roster (Leader row, member count, Manage roles link, invite button) all confirmed working on the connected physical device. Sign-out/re-entry for an existing member and invite-code generation also now verified (below).
- [x] **Manage Roles + Reassign Role verified on-device (2026-09-15, session 3), see `specs/logs/2026-09-15-manage-roles-reassign-fix-and-full-audit.md`:** create/toggle-permission/delete role all confirmed correct via Firestore REST ground truth; Leader role card confirmed locked. Reassign Role exercised against a synthetic non-Leader member (no second account available) and confirmed correct after fixing a real bug: `OfflineFirstMembershipRepository.reassignRole()` never wrote the reassignment into local Room (unlike every sibling mutation) — fixed by having `FirestoreMembershipRemoteDataSource.reassignRole()` return the updated `Membership` so the repository can upsert it, matching the offline-first pattern used everywhere else.
  - **Verified on-device (2026-09-17, session 8):** Remove Member exercised via Roster overflow menu, member removed and UI updated immediately — no stale rows, no restart needed ✓
  - **Root-caused and fixed (2026-09-17, session 8):** the stale-UI-after-mutation symptom. Two compounding bugs found, not a Room/Compose issue: ~~(A) every `OfflineFirst*Repository`'s Firestore-listener sync branch only ever `upsertAll`'d, never deleted local rows missing from a fresh snapshot, so a deleted-elsewhere row could get silently re-inserted by a lagging snapshot and never self-correct~~ **fixed, see `specs/logs/2026-09-17-fix-stale-ui-room-reconciliation.md`**; ~~(B) `RosterViewModel`/`ManageRolesViewModel`/`HomeViewModel`/`ProfileViewModel` all restarted their nested per-project Firestore listeners on every unrelated re-emission from `observeUserProjects()` (via `collectLatest` wrapping the nested `launch`es), not just when the project actually changed, widening the window for (A)~~ **fixed, see `specs/logs/2026-09-17-fix-stale-ui-listener-churn.md`**.
  - **Verified (2026-09-17, session 8), see `specs/logs/2026-09-17-phase-1-completion-verification.md`:** all on-device verification complete — stale-UI fix confirmed working (reassign-role, permission-toggle, and Remove Member mutations all reflected in UI immediately with no restart), and Remove Member exercised successfully. Phase 1 is complete.
- [x] **Full 4-module code audit (2026-09-15, session 3) — all 12 numbered items fixed as of 2026-09-17, session 7** — full list in `specs/logs/2026-09-15-manage-roles-reassign-fix-and-full-audit.md`. **Still open, tracked separately (see "Still open / not yet cleaned up" and "Next" items 4–6 in that log):** the stale-UI-after-mutation/delayed-Room-write investigation, unit tests, the Firestore emulator rules test suite, and leftover synthetic Firestore test data. Headline items: ~~`OfflineFirstRoleRepository.updateRole()` hardcodes `isLeader = false` into Room (same bug class as the reassignRole fix above)~~ **fixed 2026-09-15, session 4**; ~~Google sign-in cancel shows a spurious error~~ **fixed 2026-09-15, session 4**; ~~all auth failures collapse to one generic error message~~ **fixed 2026-09-15, session 5**; ~~Firestore exceptions/listener errors are swallowed with zero logging everywhere in the data layer~~ **fixed 2026-09-15, session 5**; ~~Roster/ManageRoles mutations don't surface failures to the UI~~ **fixed 2026-09-15, session 5**; ~~Profile screen has no error-surfacing path~~ **fixed 2026-09-16, session 6**; ~~`RoleEntity`/`MembershipEntity`/`InviteCodeEntity` have no `@Index("projectId")` despite being queried by it~~ **fixed 2026-09-17, session 7**; ~~`DataError` enum diverges from the android-error-handling skill's canonical set without approval~~ **fixed 2026-09-17, session 7**. User has decided: log now, plan and fix one-by-one in future sessions — do not fix ad hoc.
  - **Fixed (2026-09-15, session 4), see `specs/logs/2026-09-15-fix-updaterole-isleader-bug.md`:** audit bug #1. `RoleRemoteDataSource.updateRole()`/`FirestoreRoleRemoteDataSource.updateRole()` now return `Result<Role, DataError>` (mirrors `createRole`): the Firestore write switched from a full `.set()` overwrite (which was also hardcoding `isLeader = false` straight into Firestore) to a partial `.update()` of just `name`/`permissions`, then re-reads and maps the doc back, matching `reassignRole`'s pattern. `OfflineFirstRoleRepository.updateRole()` now upserts the real returned `Role` into Room via `.onSuccess { roleDao.upsertAll(listOf(it.toEntity())) }.asEmptyResult()` instead of fabricating one with `isLeader = false`. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-15, session 4), see `specs/logs/2026-09-15-fix-google-signin-cancel-error.md`:** audit bug #2. Google sign-in cancel no longer shows a spurious error. Added `DataError.Network.CANCELLED` (user-confirmed approach over a feature-specific `AuthError` type, to stay minimal and not overlap with bug #3's scope), returned from `FirebaseAuthRemoteDataSource.signInWithGoogle()`'s `GetCredentialCancellationException` catch block instead of `UNKNOWN`; `LoginViewModel.googleSignIn()`'s `onFailure` now skips updating `state.error` when the error is `CANCELLED`, so a dismissed picker is a silent no-op instead of a visible error. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-15, session 5), see `specs/logs/2026-09-15-fix-auth-error-collapse.md`:** audit bug #3. Auth failures no longer all collapse to one generic error. Added a private `Exception.toAuthDataError()` mapper in `FirebaseAuthRemoteDataSource` using Firebase Auth's exception hierarchy (`FirebaseNetworkException`→`NO_INTERNET`, `FirebaseAuthUserCollisionException`→`CONFLICT`, `FirebaseAuthInvalidCredentialsException`/`FirebaseAuthInvalidUserException`→`UNAUTHORIZED`), used in all three sign-in paths' generic catch blocks; `GoogleIdTokenParsingException` now maps to `SERIALIZATION` instead of `UNKNOWN`. **Known gap left for item #8:** weak-password/too-many-requests exceptions still fall to `UNKNOWN` — no matching `DataError.Network` case exists yet. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-15, session 5), see `specs/logs/2026-09-15-fix-firestore-silent-logging.md`:** audit bug #4. `SafeFirestoreCall.kt`'s `FirebaseFirestoreException`/generic `catch (e: Exception)` branches and both `FirestoreFlow.kt` `snapshots()` extensions' `close(error)` calls now `Log.e` the exception before returning/closing — previously zero trace was left in logcat on a Firestore failure. No behavior change, pure visibility addition. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-15, session 5), see `specs/logs/2026-09-15-fix-roster-manageroles-error-surfacing.md`:** audit inconsistency #5. `RosterViewModel`'s `removeMember`/`reassignMemberRole`/`generateCode` and `ManageRolesViewModel`'s `createRole`/`updateRole`/`deleteRole` now chain `.onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }`, matching `CreateProjectViewModel`'s existing pattern — added `error: UiText?` to both states and an `OnErrorDismissed` action to both. `RosterScreen`/`ManageRolesScreen` render an inline dismissible error banner below the `TopAppBar` (no new snackbar infra, matching `CreateProjectScreen`'s existing inline-error style), each with a new error-state `@Preview`. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-16, session 6), see `specs/logs/2026-09-16-fix-flow-result-refactor-item6.md`:** audit inconsistency #6. Investigation found the literal premise didn't hold — every offline-first repo's Firestore-sync branch already caught listener errors with an empty `.catch { }` (correct offline-first behavior, no crash risk), it just logged nothing. Presented with (a) minimal logging vs (b) a full `Flow<Result<T, DataError>>` refactor, **user chose (b)**. All six `observe*` methods across `ProjectRepository`/`MembershipRepository`/`RoleRepository`/`InviteCodeRepository` (and their offline-first impls) now return `Flow<Result<T, DataError>>`; every ViewModel collecting them (`Home`/`Roster`/`ManageRoles`/`Profile`) unwraps with `.onSuccess{}.onFailure{ error -> state.error = ... }`, never clearing already-loaded data on a transient sync failure. `HomeState`/`ProfileState` gained `error`/`OnErrorDismissed` + a banner (matching item #5's pattern); `ProjectGateViewModel`'s one-shot `.first()` was changed to skip transient `Result.Error` blips and wait for the first real Room-backed `Success`. `./gradlew assembleDebug lintDebug` both pass (after a `clean` — an initial build hit a stale-incremental-cache false-positive cascade, not a real code issue).
  - **Fixed (2026-09-17, session 7), see `specs/logs/2026-09-17-fix-room-index-item7.md`:** audit inconsistency #7, index half only. Investigation found the cascade-delete half of this item is currently moot — there is no project-deletion feature anywhere in the app (`deleteProject` exists only as an unused `RolePermissions` flag), so orphaned child rows can't occur yet; **user chose to fix indices now and leave bulk-delete-by-projectId as noted future work** rather than build dead DAO methods. Added `@Index("projectId")` to `RoleEntity`/`MembershipEntity`/`InviteCodeEntity`. Room schema export wasn't configured at all (`room.schemaLocation` missing from `app/build.gradle.kts`) so `@AutoMigration` couldn't be used out of the box — **user chose to set up schema export properly** over a `fallbackToDestructiveMigration()` shortcut: added the ksp arg, committed baseline `app/schemas/.../1.json` (generated from the pre-index schema), then bumped `AppDatabase` to version 2 with `@AutoMigration(from = 1, to = 2)`, which generated `2.json`. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-17, session 7), see `specs/logs/2026-09-17-fix-dataerror-alignment-item8.md`:** audit inconsistency #8. Compared the live `DataError.Network` enum against the android-error-handling skill's canonical set — missing `BAD_REQUEST`/`FORBIDDEN`/`NOT_FOUND`/`TOO_MANY_REQUESTS`/`PAYLOAD_TOO_LARGE`/`SERVICE_UNAVAILABLE`; extra `PERMISSION_DENIED`/`CANCELLED` not in the skill. **User chose full alignment to the skill's set**, keeping `PERMISSION_DENIED`/`CANCELLED` as documented deliberate additions (not an unflagged deviation). Added the 6 missing cases; wired `FirebaseAuthWeakPasswordException`→`BAD_REQUEST` and `FirebaseTooManyRequestsException`→`TOO_MANY_REQUESTS` into `FirebaseAuthRemoteDataSource`'s mapper, closing item #3's known "weak-password/too-many-requests fall to UNKNOWN" gap. `DataErrorToUiText.kt` gained branches for `REQUEST_TIMEOUT`/`SERIALIZATION`/`TOO_MANY_REQUESTS`/`BAD_REQUEST` (the cases the app actually produces); the remaining skill cases with no current producer stay on the generic unknown-error branch. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-17, session 7), see `specs/logs/2026-09-17-fix-login-password-visibility-state-item10.md`:** audit minor #10. `LoginScreen`'s password-visibility toggle used a local `remember { mutableStateOf(false) }` instead of ViewModel state, against android-compose-ui's state-ownership rule. Moved it to `LoginState.isPasswordVisible` + a new `LoginAction.OnTogglePasswordVisibility`, handled in `LoginViewModel`; removed the now-unused `remember`/`mutableStateOf`/`setValue` imports from `LoginScreen.kt`. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-17, session 7), see `specs/logs/2026-09-17-fix-stable-annotation-states-item11.md`:** audit minor #11. `LoginState` was missing `@Stable` despite its unstable `UiText?` field. Checked every presentation state and found the same gap in `HomeState`/`ProfileState`/`CreateProjectState`/`JoinProjectState` (same `UiText?`-only case) and `RosterState`/`ManageRolesState` (already unstable via `List<Membership>`/`List<Role>`) — **user chose to fix all 7** rather than just the one item literally named. Added `@Stable` to all seven. `./gradlew assembleDebug lintDebug` both pass.
  - **Fixed (2026-09-17, session 7), see `specs/logs/2026-09-17-fix-style-cleanup-item12.md`:** audit minor #12. Removed the unused `Stone` design-system color token; condensed the two multi-line comment blocks in `CoreModule.kt` and `CurrentActivityHolder.kt` down to one line each, matching CLAUDE.md's comment-style rule. `./gradlew assembleDebug lintDebug` both pass.
  - **In progress (2026-09-17, session 7), audit item #9 (missing preview coverage), landing as 4 separate commits:** ~~`ProjectGateScreen` had no Root/Screen split and no `@Preview`~~ **fixed, see `specs/logs/2026-09-17-fix-project-gate-preview-item9a.md`** — extracted a stateless `ProjectGateScreen()` composable and added a preview. ~~`RosterScreen` covered only 1 of ~5 states~~ **fixed, see `specs/logs/2026-09-17-fix-roster-preview-item9b.md`** — added loading, invite-sheet, and reassign-dialog previews. ~~`ManageRolesScreen` was missing loading/empty-role-list previews~~ **fixed, see `specs/logs/2026-09-17-fix-manage-roles-preview-item9c.md`**. ~~`HomeScreen` was missing an empty-state preview~~ **fixed, see `specs/logs/2026-09-17-fix-home-preview-item9d.md`**. **Item #9 complete — all 4 sub-items fixed 2026-09-17, session 7.**
- [ ] Unit tests (ViewModel tests per phase-1 spec's Testing section — create/join project, permission toggles, Leader immutability) — **deferred, not part of Phase 1 completion per user instruction** (2026-09-17, session 8).
- [x] Firestore security rules written (`firestore.rules`, repo root) for projects/roles/members/inviteCodes, matching the actual field shapes in `feature_project/data/mapper/*Mapper.kt` and the exact reads/writes in `feature_project/data/remote/*RemoteDataSource.kt`. Enforces: read access gated on project membership; role/member creation validated against `RolePermissions.ALL_GRANTED`/`NONE`; role reassignment and invite-code management gated on the matching `RolePermissions` flag; the Leader role/membership can never be edited, deleted, or reassigned. **Known accepted gap:** `createProjectWithLeader()`'s project+role+member `WriteBatch` is evaluated by Firestore against pre-batch state per write, so true cross-document integrity isn't enforceable there without a Cloud Function (Admin SDK) — the rules instead gate on "project doc doesn't exist yet," which limits a malicious signed-in user to spamming extra docs inside their own brand-new project, nothing more.
  - **Deployed (2026-09-15):** `firebase.json`/`firestore.indexes.json` added (repo had neither), CLI linked to `temacker-a0252`, rules deployed.
  - **Firestore emulator + Jest rules test suite added (2026-09-17, session 8), see `specs/logs/2026-09-17-add-firestore-rules-test-suite.md`:** 45 test cases across every collection, built from scratch (`package.json`, `.firebaserc`, `firebase.json` emulator config, `firestore-tests/rules.test.js`). Found and fixed a real gap: the `members` create rule's join-existing-project branch never excluded `isLeader == true` (unlike the update path, which already did), so anyone who knew a project's Leader role doc ID could self-assign themselves as Leader. Fixed and **deployed to production**. All 45 tests pass (`npm run test:rules`).
  - **Fixed (2026-09-15), found via on-device testing, see `specs/logs/2026-09-15-firebase-cli-deploy-and-golden-path-fixes.md`:** (1) `fullPermissions()`/`noPermissions()` map literals needed quoted string keys — bare identifiers parse as variable references and silently fail, denying every permission check depending on them. (2) `members` read rule needed `|| resource.data.userId == request.auth.uid` — Firestore validates collection-group queries from the query's own filter alone, not by running `isMember()`'s `exists()` per result document across a variable `projectId`.
  - **Fixed again (2026-09-15, session 2), see `specs/logs/2026-09-15-invite-code-and-collection-group-rule-fix.md`:** bug (2)'s OR-based fix never actually worked — Firestore's list/collectionGroup query validator rejects the *entire* rule if it contains any `exists()`/`get()` call it can't resolve, even behind an OR with an otherwise-provable branch. Fixed properly with a dedicated top-level `match /{path=**}/members/{userId} { allow list: if resource.data.userId == request.auth.uid; }` block specifically for the collectionGroup query, leaving the nested `projects/{projectId}/members` rule (used by roster's direct query, where `isMember(projectId)` *is* resolvable) unchanged. Also found and fixed the same class of bug in `inviteCodes`'s `allow list` rule (`generateInviteCode()`'s existing-active-code lookup was silently `PERMISSION_DENIED`, swallowed by the ViewModel with no error surfaced) — `inviteCodes` isn't nested under `/projects/{projectId}`, so there's no resource.data-only substitute; loosened to `allow list: if isSignedIn()` (same trust boundary as the existing `allow get`). **Flagging per CLAUDE.md rule 2:** this is a real security loosening (any signed-in user can now list invite codes for a project whose ID they already know, not just members) — revisit if this needs tightening, e.g. by moving `inviteCodes` under `projects/{projectId}/inviteCodes`.
- [x] **Fixed (2026-09-15):** `observeUserProjects()` always returned empty — it read a `projectId` field off member docs that `MembershipMapper` never writes (implicit in the doc path); now derives it from `it.reference.parent.parent?.id`. Also, `createProjectWithLeader()` now returns the Leader `Membership` alongside `Project` so `OfflineFirstProjectRepository.createProject()` can seed the local `membershipDao` row immediately (previously only `joinProject()` did this) — without it, a freshly created project never appeared in the UI until something else happened to populate that row.

## Phase 2 — Board and the baton (`phase-2-board-baton.md`)
- [x] **Commit gap found and fixed (2026-09-18):** this file marked Phase 2 complete while the entire
  `feature_tasks` module (55 files), the bottom-nav shell rewrite, both cross-feature providers,
  `AppDatabase` v3, and the architecture doc rewrite were sitting uncommitted — only the Firestore
  rules/tests and two device-verification bugfixes had actually landed in git. Verified
  `./gradlew assembleDebug lintDebug` passed on the uncommitted tree, then split it into 5 atomic
  commits matching the repo's existing style: `96d5b24` (docs: architecture rewrite), `d310726` (feat:
  feature_tasks domain+data layer), `b2a86df` (feat: cross-feature providers), `fc55a26` (feat:
  feature_tasks screens), `b9a648f` (refactor: bottom nav shell). Re-verified build/lint clean at HEAD
  after the split.
- [x] Rewrite architecture doc's Data Model + Phase Roadmap to holder/handoff model (2026-09-17). Also
  added two new cross-feature contracts to §8 (`ProjectMemberProvider`, `CurrentProjectProvider` in
  `core/domain`, implemented in `feature_project/data`) — `feature_tasks` needs the member list,
  the current user's `assignTasks`/`editAnyTask` flags, and the current project id, none of which
  existed outside `feature_project`'s domain before. Flagged per CLAUDE.md rule 2.
- [x] feature_tasks scaffold + domain (`Task`, `Handoff`, `TaskRepository`, 12 use cases: create/delete
  task, offer/accept/decline handoff, mark done, observe board/task/trail/pending-handoffs/current
  project id/current project member/project members) (2026-09-17).
- [x] feature_tasks data layer: `FirestoreTaskRemoteDataSource` (transactional accept/decline/offer,
  matching `reassignRole`'s "return the updated doc so the repo can upsert Room immediately" pattern),
  `TaskEntity`/`HandoffEntity`/DAOs (`@Index("projectId")` + `deleteMissing` from day one, per the
  Phase 1 stale-UI lesson), `OfflineFirstTaskRepository` (channelFlow sync-then-read-Room, same shape
  as `OfflineFirstMembershipRepository`). `AppDatabase` bumped to v3 with `@AutoMigration(2,3)`
  (2026-09-17).
- [x] Screens: Board (tabs + waiting-on-you strip + FAB), New task, Task detail + baton trail
  (hand off/mark done/hand back/delete menu), Hand-off sheet, Handoff-to-you (Incoming), Decline
  (reason required, quick chips) — all Root/Screen split, MVI, `@Preview`s for loading/empty/populated
  states. Read-only board (no FAB when `assignTasks` is false) and delete confirm dialog folded into
  Board/TaskDetail rather than separate screens. (2026-09-17)
- [x] Bottom nav shell (Board · Team · You) (2026-09-17): `AppDestination` enum renamed
  `HOME/ROSTER/PROFILE` → `BOARD/TEAM/YOU`; `HomeScreen`/`HomeViewModel`/`HomeRoute` retired per
  user decision (Board replaces Home as the first tab); `RosterScreen`/`ProfileScreen` relabeled to
  Team/You in place (same screens, same ViewModels, renamed nav callbacks only).
- [x] Permission gating on board actions (2026-09-17): `assignTasks` gates task creation (the FAB);
  `editAnyTask` gates deletion of any task regardless of who holds it; accepting/declining/handing off
  a task you currently hold never requires a permission. Decided with the user — no new
  `RolePermissions` field added.
- [x] Task status derivation (2026-09-17): no manual status picker (matches the mock, which has none).
  `TODO` at creation, auto-flips to `DOING` on first accepted handoff, `DONE` via a new holder-only
  "Mark done" action (small addition beyond the literal mock, confirmed with the user).
- [x] Firestore security rules for tasks/handoffs/events (2026-09-17): nested `projects/{id}/tasks`,
  `.../tasks/{id}/handoffs`, `projects/{id}/events` (write-only, unread until Phase 4), plus a
  collection-group `allow list` rule for `observePendingHandoffs()`'s cross-task query (same pattern
  Phase 1 needed for `inviteCodes`/`members`). 17 new Jest/emulator test cases added to
  `firestore-tests/rules.test.js` (62/62 passing). **Deployed to production (2026-09-17)** —
  `firebase deploy --only firestore:rules` succeeded against `temacker-a0252` after a CLI re-login
  (the stored token had expired, 401 on first attempt).
- [ ] Unit tests for handoff transitions and status derivation (`OfferHandoff`/`AcceptHandoff`/
  `DeclineHandoff`, `TODO→DOING→DONE`) — **deferred, matching Phase 1's precedent of deferring
  ViewModel unit tests** (2026-09-17); flagged to the user rather than assumed.
- [x] **On-device golden-path verification (2026-09-18), see `specs/logs/2026-09-18-phase-2-device-verification.md`:**
  create → mark done → delete all confirmed working on the connected physical device via adb-driven
  UI automation. Task created as TODO, held by creator (Rudra Sharma); "Mark done" flipped it straight
  to DONE (no intermediate handoff, correctly bypassing DOING since status derives from holder
  history); delete showed the correct confirmation copy ("...handoff history will be gone
  permanently...") and removed it, all tab counts returned to 0. The earlier "task creation fails
  silently" finding (previous session) was a **false alarm** — root cause was adb tap coordinates
  computed from the scaled screenshot-display size instead of raw device pixels (1080×2340), landing
  taps on the wrong elements; not an app bug.
  **Full golden path completed 2026-09-18 (session 2)** after adding a second real member
  (`FAIQUA NAEEM`, Default role, joined via invite code): hand-off offer/accept confirmed
  (TODO→DOING, holder changes, baton trail records "Offered to X — waiting" then the acceptance);
  hand-off offer/decline confirmed (holder stays with offerer, status stays TODO, baton trail
  records "X declined — <reason>"); Default-role read-only board confirmed (no FAB, no "Add the
  first task" CTA); permission gating on task detail confirmed (a non-holder without `editAnyTask`
  sees zero action buttons — no Hand off/Mark done/Delete). **Two real bugs found and fixed along
  the way, both deployed to production:**
  1. `OfflineFirstInviteCodeRepository` never deactivated the previous invite code row in Room when
     a new one was generated (only Firestore was updated), so `observeActiveForProject`'s
     `LIMIT 1` query with no `ORDER BY` could return a stale, already-expired code — visible
     on-device as "Generate new code" appearing to do nothing.
  2. `joinProject()`'s transaction read `projects/{projectId}` and
     `projects/{projectId}/roles/{roleId}` before the joiner was a member, but both rules required
     `isMember(projectId)` — a chicken-and-egg gap that made joining an existing project via invite
     code impossible for a genuinely new account (previously masked because Phase 1's second-member
     testing used a synthetic Firestore-seeded member, never a real join). Fixed by splitting
     `get`/`list` on both rules — `get` now allows any signed-in user (needed for the join
     transaction; these docs hold no sensitive data), `list` stays member-only.
  See `specs/logs/2026-09-18-phase-2-device-verification.md` for full details, and
  `specs/logs/2026-09-18-invite-code-and-join-rules-fix.md` for the two bug fixes.

## Phase 3 — Notifications, honest offline (`phase-3-notifications-offline.md`)
Started 2026-09-19, see `specs/logs/2026-09-19-phase-3-backend-and-inbox.md`.
- [x] Spec + architecture additions agreed and applied (Step 0).
- [x] Backend (Step 1): fcmTokens rule + 5 tests, `functions/` (`onHandoffWritten`, `nudgeStaleHandoffs`, 9 Jest tests), nudge index — rules/indexes/functions deployed. Runtime IAM granted; trigger runs clean and the nudge stamps `nudgedAt` (verified with a synthetic stale offer). **Open:** send-logging fix committed but not redeployed; a real FCM send is unverified until Step 3 registers a device token.
- [x] FCM setup + permission rationale screen (Step 3) — verified on device, see `specs/logs/2026-09-19-phase-3-fcm-client.md`
- [x] Lock-screen notification style (Step 3) — BigText, Accept/Decline, private + public version; all five push types verified on device; lock-screen pixels not eyeballed (One UI hides them; posted notification is private + public version)
- [x] Inbox nav destination + badge (Step 2) — built, 8 unit tests, verified on-device (waiting/earlier sections, badge, open + accept). Rules/index for the "sent by me" query deployed.
- [x] Offline queue (WorkManager) (Step 4) — Room v6 outbox, connectivity observer, replayer/worker, Board strip + Queued chip + Undo snackbar, queue screen with Retry/Discard; 52 unit tests; verified on device incl. a forced conflict. See `specs/logs/2026-09-19-phase-3-offline-outbox.md`

## Phase 4 — Truth about the team (`phase-4-team-truth.md`)
Built ahead of Phase 3 per explicit user request (2026-09-18) — normally blocked on Phase 3's data
existing first, per this file's own note; accepted since the underlying Phase 1/2 handoff/task data
Load/Stuck/Pulse read already exists and doesn't depend on notifications.
- [x] Domain model: `Project.isArchived`/`predecessorProjectId`, `Membership.isLeader` (denormalized,
  replaces the old `roleName == "Leader"` string-compare), `Event`/`EventEntity`/`EventDao` (Room
  mirror — the architecture doc's old "no Room mirror for events" line needs updating, not done yet).
- [x] `TeamInsightsProvider` cross-feature contract (feature_tasks owns the impl, feature_project
  consumes — first contract flowing this direction, see `TaskTeamInsightsProvider`).
- [x] Load / Stuck / Pulse tabs inside Team — `TeamScreen` (TabRow host, Roster re-hosted as a tab)
  + `LoadScreen`/`StuckScreen`/`PulseScreen`, each full MVI with loading/empty/error/populated previews.
- [x] Aggregation use cases (`ObserveLoadUseCase`/`ObserveStuckHandoffsUseCase`/`ObservePulseUseCase`).
- [x] Succession (end-of-cycle) flow — `TriggerSuccessionUseCase` (Leader-only, archives the old
  project, clones the full roster including custom roles into a new project via one Firestore batch
  + one Room transaction), `SuccessionScreen` (name → confirm → submit, nav-graph-gated to Leader).
- [x] Firestore rules: events read/type-allowlist, archival-flip update rule (`hasLeaderRole`,
  diff-key-restricted), roster-copy create rules for roles/members (gated on a rule-validation-only
  `predecessorProjectId` field) — 78/78 `npm run test:rules` passing, including 15 new succession tests.
- [ ] Unit tests for the aggregation use cases/ViewModels and `TriggerSuccessionUseCase` — deferred,
  matching the explicit precedent set in Phase 1/2 (rules tests + preview coverage only). If this
  should change for Phase 4 specifically, say so — the plan flagged this as an open question that was
  never explicitly answered.
- [x] On-device golden path (physical device, both accounts, 2026-09-19): Room v3→v5 migration,
  Roster/Load/Stuck/Pulse render real data, `TASK_MARKED_DONE` event written under the deployed rules
  and shown in Pulse, Succession affordance hidden for the non-Leader and shown for the Leader,
  full Leader flow (name → confirm → submit) archived the old project and created "Cycle2" with the
  roster (both members, Leader preserved) and an empty Board. Rules deployed to `temacker-a0252`.
  Not exercised on-device: a custom (non-Leader/Default) role carrying over — covered by rules tests only.
  Found + fixed on-device: pre-Phase-4 member/project docs lack `isLeader`/`isArchived` (commit
  "fix: tolerate pre-Phase-4 docs").
  The stale "This code expired" banner on every screen was two missing Firestore indexes (members
  collection-group `userId` exemption, handoffs composite `projectId`+`status`+`toUid`), whose
  FAILED_PRECONDITION errors `toFirestoreDataError()` maps to CONFLICT and the UI shows as that
  invite-code text. Now tracked in `firestore.indexes.json` and deployed. 
  Follow-ups fixed: CONFLICT now shows a neutral message (invite-specific text only in `JoinProjectViewModel`);
  SuccessionScreen uses `imePadding()` so Continue rides above the keyboard (verified on-device).
  `CreateProjectScreen` has the same button-under-keyboard layout — not touched, out of Phase 4 scope.

## Phase 5 — v1.0, used by others (`phase-5-v1-others.md`)
- [x] Invited-member first-run screen — reached only from the join flow, verified on device (2026-09-22), see `specs/logs/2026-09-22-phase-5-first-run-and-role-explainer.md`
- [x] Role explainer screen (tap a roster row) + who-set-the-role data (Room v7, rules deployed, 92 rules tests, 59 unit tests)
- [x] Project switcher — spun out into its own phase, `phase-5b-project-switcher.md` (below)

## Phase 5b — Project switcher (`phase-5b-project-switcher.md`)
- [x] `SelectedProjectStore` (DataStore) + `ProjectCurrentProjectProvider` rewrite (fallback + persist), 4 unit tests
- [x] Every `feature_project` ViewModel that bypassed `CurrentProjectProvider` switched to it (Roster, ManageRoles,
  Load, Stuck, Pulse, RoleExplainer, InvitedFirstRun, Succession); `JoinProjectViewModel` selects the newly joined project
- [x] Switch-project screen (MVI, previews) + `AppScaffold` header slot + `SwitcherPill`, wired on Team and You
- [x] `MainActivity` notification-tap now auto-switches instead of dropping the tap
- [x] Device-verified: pill renders correctly on Team/You, status-bar overlap bug found and fixed, no crashes
- [ ] **Not yet device-verified:** the actual multi-project switch tap-through and the notification auto-switch —
  blocked on a safe way to get the test account into a genuine multi-project state (see phase-5b spec's "On-device
  verification" section)
- [ ] Board/Inbox entry point — flagged follow-up, needs a project-name cross-feature contract `feature_tasks` doesn't have today

## Phase 6 — Sustainability (`phase-6-sustainability.md`)
- [ ] Your data (export + delete-account)
- [ ] Plan & limits screen

## Open decisions
- Phase 5b: get the device into a genuine multi-project state (safely) to finish verifying the switch tap-through and notification auto-switch.
- Phase 5b follow-up: Board/Inbox switcher entry point needs a project-name cross-feature contract (not built).
- Phase 6: extend `feature_profile` vs. new `feature_settings` — default is extend, revisit if it grows.
