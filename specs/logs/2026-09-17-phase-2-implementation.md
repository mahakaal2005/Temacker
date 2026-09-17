# 2026-09-17 — Phase 2 implementation: Board and the baton

## Scope

Full Phase 2 build: holder/handoff task model, `feature_tasks` (domain/data/presentation/di),
bottom nav shell (Board · Team · You, replacing Home), Firestore security rules, and the
architecture-doc rewrite that was the phase's own prerequisite.

## Plan-mode decisions (confirmed with the user before implementation)

1. **Bottom nav**: Board replaces Home as the first tab. Team = Roster (relabeled). You = Profile
   (relabeled). `HomeScreen`/`HomeViewModel`/`HomeRoute` deleted.
2. **Task permissions**: no new `RolePermissions` field. `assignTasks` gates task creation (FAB).
   `editAnyTask` gates deleting any task, regardless of who holds it. Accepting/declining/handing off
   a task you currently hold never needs a permission.
3. **Task status**: no manual picker (mock has none). `TODO` at creation, auto-flips to `DOING` on the
   first accepted handoff, `DONE` via a new holder-only "Mark done" action.

## Architecture changes (specs/ultimate_android_architecture.md)

- Data Model section rewritten: `Task`/`Handoff`/`Event` now describe the holder/handoff shape
  (`projects/{id}/tasks/{id}`, nested `handoffs` subcollection, project-scoped `events` collection),
  replacing the old Backlog/In Progress/Review/Done model.
- Phase Roadmap section rewritten to match the current 6-phase breakdown in `specs/office/`.
- §8 (Cross-Feature Communication) gained two new contracts, both following the existing
  `SessionManager` pattern (core owns the interface, a feature owns the implementation):
  - `core/domain/repository/ProjectMemberProvider` — member list + the current user's
    `assignTasks`/`editAnyTask` flags, via a trimmed `ProjectMember` model (not `feature_project`'s
    full `RolePermissions`). Implemented by `feature_project/data/repository/MembershipProjectMemberProvider`.
  - `core/domain/repository/CurrentProjectProvider` — "the user's current project id", replacing the
    `observeUserProjects().firstOrNull()?.id` logic every Phase 1 ViewModel duplicated inline.
    Implemented by `feature_project/data/repository/ProjectCurrentProjectProvider`.

These are real additions beyond the original plan (the plan only named `ProjectMemberProvider`), added
because Board/NewTask/TaskDetail/Handoff/Incoming/Decline all independently need "what project am I
in" the same way every Phase 1 screen did — flagged here per CLAUDE.md rule 2 rather than silently.

## feature_tasks

- **Domain**: `Task`/`Handoff`/`TaskStatus`/`HandoffStatus` models, `TaskRepository` interface, 12 use
  cases (straight pass-throughs, matching `feature_project`'s convention).
- **Data**: `FirestoreTaskRemoteDataSource` — transactional `acceptHandoff`/`declineHandoff`/
  `offerHandoff` (read-modify-write via `firestore.runTransaction`, mirroring `reassignRole`'s
  pattern of returning the updated doc so the repository can upsert Room immediately); `deleteTask`
  batches the task delete with an `events` doc write (audit record, per the mock's "the project keeps
  an attributable event record"). `TaskEntity`/`HandoffEntity` + DAOs went in with `@Index("projectId")`
  and `deleteMissing`/`deleteMissingForTask`/`deleteMissingPendingForUser` from day one — Phase 1 hit
  a real stale-UI bug from skipping this on the first pass, not repeating it here.
  `OfflineFirstTaskRepository` follows `OfflineFirstMembershipRepository`'s channelFlow
  sync-then-read-Room shape exactly. `AppDatabase` bumped v2→v3 with `@AutoMigration(2, 3)`.
- **Presentation**: Board (tabs + "waiting on you" strip + FAB, gated on `assignTasks`), New task,
  Task detail + baton trail (Hand off / Mark done / Hand back to previous holder / Delete, gated on
  `editAnyTask`), Hand-off sheet (member picker + note), Incoming ("Handoff to you", accept/decline),
  Decline (required reason + quick chips). All Root/Screen split, MVI, `@Preview`s for
  loading/empty/populated states. Screens needing a runtime nav param (`taskId`, `handoffId`) take it
  as a constructor parameter resolved via Koin's lambda `viewModel { (id) -> ... }` form + `koinViewModel(parameters = { parametersOf(...) })`
  in the Root composable — the documented fallback in the android-di-koin skill for params a
  constructor reference alone can't resolve.
- **DI**: `TasksModule.kt`, registered in `App.kt`'s `startKoin`.
- **Nav**: `TasksRoutes.kt`/`TasksGraph.kt`, wired into `MainActivity.kt`'s `NavHost` alongside
  `projectGraph`/`profileGraph`. `ProjectGraph.kt` gained an `onNavigateToBoard` cross-feature callback
  (Board lives in `feature_tasks`, not `feature_project`).

## Bottom nav retrofit

`AppDestination` enum (`core/presentation/components/AppScaffold.kt`) renamed
`HOME/ROSTER/PROFILE` → `BOARD/TEAM/YOU`. `RosterScreen.kt`/`ProfileScreen.kt` had their nav callback
parameter names mechanically renamed to match (`onNavigateToHome`→`onNavigateToBoard`,
`onNavigateToRoster`→`onNavigateToTeam`, `onNavigateToProfile`→`onNavigateToYou`) — same screens, same
ViewModels, no behavior change beyond the new destination they route to. `feature_project/presentation/home/`
deleted outright (`HomeScreen`/`HomeState`/`HomeAction`/`HomeEvent`/`HomeViewModel`).

## Firestore rules

Added nested `tasks`/`handoffs` rules under `projects/{projectId}` and a project-scoped `events`
collection (write-only, unread until Phase 4's Pulse tab needs it), plus a collection-group `allow
list` rule for `observePendingHandoffs()`'s cross-task query — same shape as the `members`/`inviteCodes`
collection-group fixes from Phase 1. `firestore-tests/rules.test.js` extended with 17 new cases (62/62
passing against the emulator). **Deployed to production** — first attempt was blocked by the
auto-mode classifier as a production action; user explicitly asked for it in a follow-up, first
`firebase deploy` attempt then hit a 401 (expired CLI token), user re-ran `firebase login --reauth`,
redeploy succeeded against `temacker-a0252`.

## Verification

- `./gradlew clean assembleDebug lintDebug` — green, zero new lint errors.
- `npm run test:rules` — 62/62 passing.
- On-device golden path — **not run this session** (same device-connectivity gap noted in Phase 1's
  early sessions). Next session should verify: create task → hand off → accept (status TODO→DOING) →
  mark done → delete (editAnyTask) → Default-role read-only board (no FAB, "ask X for a role" strip).

## Deferred / open

- ViewModel/use-case unit tests (`OfferHandoff`/`AcceptHandoff`/`DeclineHandoff` transitions,
  `TODO→DOING→DONE` derivation) — deferred, matching Phase 1's precedent of deferring this class of
  test. Flagged to the user rather than assumed; revisit if Phase 2 needs to close this debt.
- **On-device golden-path verification (2026-09-18)** — **corrected below.** The "task creation
  fails silently" finding logged here was a false alarm caused by wrong adb tap coordinates
  (computed from scaled screenshot display size instead of raw device pixels), not an app bug.
  Create/mark-done/delete all verified working on 2026-09-18 — see
  `specs/logs/2026-09-18-phase-2-device-verification.md`. Hand-off/accept/decline and the
  Default-role read-only board still need a second project member to test.
