# Phase 5b — Project Switcher

Status: complete, fully verified on device end-to-end (2026-09-22, session 3).

Closes the gap Phase 5 deliberately left open: "current project" is the first project the user belongs to
(`CurrentProjectProvider`), so someone in two projects can't see the second, and a push for a non-current
project is dropped on tap. One new screen plus real selection state.

## Screens / Flows

- **Switch project** — list of the user's projects (name, member count, the viewer's role in that project, e.g.
  "8 members · Leader" per the concept HTML's `switcher-meta` line), current selection marked, tap another row to
  select and pop back. Opened from a new app-bar header. A "Join another project" button at the bottom opens the
  existing `JoinProjectScreen` — this is the only way any account can ever reach a second project, see below.
- **App bar switcher pill** — project name + meta line; always tappable (opens Switch Project), chevron shown
  only when the user belongs to 2+ projects.
- **Entry point shipped on all four tabs: Team, You, Board, Inbox.** `CurrentProjectProvider` gained
  `observeCurrentProjectSummary()` (returns the new `ProjectSummary` domain model: name, member count, viewer's
  role name, `hasOtherProjects`) so `feature_tasks` no longer needs `feature_project`'s domain to show the pill —
  it consumes `ProjectSummary` via a new `ObserveCurrentProjectSummaryUseCase` in its own module. `BoardState`/
  `InboxState` each gained a `projectSummary` field collected in their ViewModels; `BoardScreen`/`InboxScreen`
  wire it into `AppScaffold`'s `header` slot exactly like Team/You do. Verified on device 2026-09-23: pill renders
  correctly on both screens (no status-bar overlap), tapping it opens Switch Project, and switching from there
  updates Board and Inbox immediately.

## Agreed decisions (2026-09-22)

- **Entry point: `AppScaffold` top bar.** New optional `header` slot, default `null` so every existing call site
  keeps compiling until updated.
- **Notification tap for a non-current project: auto-switch then navigate.** Persist that project as selected,
  then open the route — replaces today's silent drop in `MainActivity.kt`.
- **Single-project users: hide the chevron only, keep the tap target.** Originally scoped as "hide the whole
  affordance," but implementation found `JoinProjectRoute` was reachable only from `NoProjectRoute` (i.e. only at
  zero projects) — there was no way for anyone, ever, to join a second project through the UI. Fixed by adding a
  "Join another project" entry point on the new Switch Project screen and keeping the pill always tappable (only
  the chevron — the "there's a list" affordance — is conditional on `hasOtherProjects`). This is the actual
  mechanism by which a user reaches 2+ projects in the first place, not just cosmetic.
- **Stale selection: fall back to first project.** If the persisted selected id isn't in the live project list
  (e.g. removed from that project), `CurrentProjectProvider` falls back to `.firstOrNull()?.id` and persists that.
- **No Room/Firestore/rules change.** Selection is client-only DataStore state, not persisted project data.
- **Every `feature_project` screen's "current project" now goes through `CurrentProjectProvider`.** Investigation
  during implementation found `RosterViewModel`, `ManageRolesViewModel`, `LoadViewModel`, `StuckViewModel`,
  `PulseViewModel`, `RoleExplainerViewModel`, `InvitedFirstRunViewModel`, and `SuccessionViewModel` all derived
  "current project" with their own inline `observeUserProjects().firstOrNull()?.id`, bypassing the contract the
  switcher writes to — so switching would have silently done nothing for the entire Team tab. Widened this pass
  (user-approved) to fix all eight; only `ProjectGateViewModel` is untouched (it checks "does the user have any
  project", not "which one"). `JoinProjectViewModel` also now calls `SelectedProjectStore.setSelectedProjectId`
  on a successful join, so joining a second project while already in one makes the new project current (otherwise
  the invited-first-run screen would have kept showing the old selection).

## Data model / architecture changes (approved with this spec)

1. New interface `SelectedProjectStore` (`core/domain`): `observeSelectedProjectId(): Flow<String?>`,
   `suspend fun setSelectedProjectId(id: String)`. Implemented with the existing shared `DataStore<Preferences>`
   singleton, following `DataStoreSessionManager`'s pattern exactly (one `stringPreferencesKey`).
2. `ProjectCurrentProjectProvider` rewritten to combine `SelectedProjectStore` with `observeUserProjects()`,
   resolving to the selected id if present in the list, else the first project's id (persisting the fallback).
   `CurrentProjectProvider`'s own interface signature is unchanged.
3. One new route in `feature_project`: `SwitchProjectRoute` (data object).
4. `AppScaffold` gains an optional `header: (@Composable () -> Unit)? = null` parameter.
5. `MainActivity`'s notification-tap gate gets an `else` branch: persist the new selection before navigating,
   instead of dropping the tap.
6. `MembershipRemoteDataSource.joinProject()` now returns `Result<Pair<Membership, Project>, DataError>` (was
   `Result<Membership, DataError>`) so `OfflineFirstMembershipRepository` can seed `projectDao` as well as
   `membershipDao` on join — found necessary by a real race bug during device testing (see session 3 below).
   `MembershipRepository.joinProject()`'s own signature is unchanged; the Pair is unwrapped inside the repo.
7. `FirebaseAuthRemoteDataSource.signInWithGoogle()` always uses the full `GetSignInWithGoogleOption` account
   chooser now, dropping the `filterByAuthorizedAccounts = true` fast path — it was silently hiding every device
   account except ones already used with this app. Not part of the switcher itself, but needed to test it and a
   real UX bug on its own.

## Files (representative)

- `core/domain/repository/SelectedProjectStore.kt` (new interface)
- `feature_project/data/repository/{DataStoreSelectedProjectStore,ProjectCurrentProjectProvider}.kt`
- `feature_project/presentation/switch_project/*` (new MVI screen)
- `core/presentation/components/AppScaffold.kt` (header slot)
- `feature_project/presentation/navigation/{ProjectRoutes,ProjectGraph}.kt`
- `MainActivity.kt` (notification-tap auto-switch)
- `feature_tasks/presentation/{board,inbox}/*Screen.kt`, `feature_project/presentation/team/TeamScreen.kt`,
  `feature_profile/presentation/*/ProfileScreen.kt` (each passes the new header)

## Testing

- Pure logic: fallback resolution (selected id missing from list → first project, persisted) with fake
  `SelectedProjectStore` + fake project list.
- ViewModel unit tests for the new screen stay deferred, matching Phases 1, 2, 4, 5.
- Previews for every state: switch-project screen (2 projects, loading, error), app-bar header (with/without
  chevron).
- On device (RZCWA28EAZF, both accounts, one added to a second project for this test): switcher hidden with one
  project, appears with two; switching updates Board/Inbox/Team immediately; relaunch keeps the switched
  selection; a push for the non-selected project switches to it and opens the right screen; losing membership in
  the selected project falls back to the remaining one.

## On-device verification (2026-09-22)

Confirmed on RZCWA28EAZF (Rudra Sharma, Leader on Cycle2):
- Fresh install, build/lint/unit tests all pass; app launches and runs with no crash.
- The pill renders correctly on Team ("Cycle2 · 2 members · Leader") and on You ("Cycle2 · 2 members · Leader"),
  both matching existing screen content (Roster list, Profile card) unaffected by the change.
- **Found and fixed a real bug (session 1):** `SwitcherPill` sat under the status bar, overlapping the
  clock/battery icons — it's a plain composable placed in `Scaffold`'s `topBar` slot, which (unlike `TopAppBar`)
  doesn't consume the status-bar inset on its own. Fixed with `Modifier.windowInsetsPadding(WindowInsets.statusBars)`
  on the pill's `Surface`. Re-verified clean on device after the fix.
- **Found and fixed a bigger structural gap (session 2):** `JoinProjectRoute` was reachable only from
  `NoProjectRoute`, itself reachable only at zero projects — meaning no account could ever reach a second active
  project through the UI, making the switcher untestable and, more importantly, unusable for real users. Fixed by
  adding a "Join another project" button to the Switch Project screen and making the pill always tappable (the
  chevron alone stays conditional on `hasOtherProjects`). Verified on device: tapping the pill (with no chevron,
  single project) correctly opens Switch Project, showing Cycle2 with a check mark and the new button; tapping it
  opens the real `JoinProjectScreen`; Back from there and Back from Switch Project both correctly return to Team,
  nav stack intact.
- Single-project state (the account's real, current state) correctly shows no chevron but is tappable.

### Session 3 — full multi-project flow, and two more real bugs found and fixed

The Google sign-in picker was silently limiting itself to accounts already authorized for this app
(`GetGoogleIdOption(filterByAuthorizedAccounts = true)` matched immediately, so the code never fell through to the
full-picker branch) — pointed out directly by the user ("why are you using the samsung account picker"). Fixed
`FirebaseAuthRemoteDataSource.signInWithGoogle()` to always request `GetSignInWithGoogleOption`, the real Android
"Choose an account" chooser listing every Google account on the device, dropping the authorized-accounts-first
short-circuit entirely. Verified on device: the full chooser now lists all six device accounts, confirmed sign-in
into a genuinely unused one.

With a real fresh account, built the intended second-project path fully: signed in as that account, created
"Switcher Test Project" via the real `CreateProjectScreen` (Leader), generated an invite code, signed back in as
Rudra, and used Team's pill → Switch Project → "Join another project" → the code → real `JoinProjectUseCase`.

That surfaced a real race: the invited-first-run screen showed **"You're in — Cycle2"** — the wrong project —
right after joining "Switcher Test Project". Root cause: `JoinProjectViewModel` correctly calls
`SelectedProjectStore.setSelectedProjectId(newProjectId)` on success, but `OfflineFirstMembershipRepository
.joinProject()` only seeded `membershipDao`, not `projectDao` — mirroring `createProject()`'s existing pattern only
halfway. `ProjectCurrentProjectProvider`'s fallback logic then saw the new id wasn't (yet) in the still-stale
project list from `observeUserProjects()`, treated it as an invalid selection, and silently overwrote it back to
the old project — the same self-correcting logic built for genuinely stale selections, tripped by a sync race
instead. Fixed by having `MembershipRemoteDataSource.joinProject()` also return the joined `Project` (the
transaction already reads it) and `OfflineFirstMembershipRepository.joinProject()` seed `projectDao` immediately,
symmetric to `createProject()`. Re-verified: first-run screen now correctly shows the just-joined project.

Also found, while re-testing sign-out, a transient **"You don't have permission to do that."** error flash on the
Profile screen during sign-out — reproduced twice. Cause: `ProfileViewModel`'s new `observeMembers`/
`observeCurrentMembership`/project listeners (added this session for the pill) are still attached when
`authRepository.signOut()` revokes the auth token, so their next Firestore snapshot briefly fails
`PERMISSION_DENIED` right as the screen is navigating away. Fixed with an `isSigningOut` flag that suppresses
`.onFailure` state updates once sign-out has started. Re-verified: sign-out and re-sign-in are now clean, no flash.

Full flow then verified clean end-to-end: switcher hidden→shown correctly as project count changed, switching
from Cycle2 to Switcher Test Project updated Team (real roster, real role "Default") and Board (empty board, no
FAB — correct for a non-`assignTasks` role) immediately, the selection survived a full app kill and relaunch, and
switching back to Cycle2 worked cleanly, leaving the account exactly as it started (Leader, Cycle2, "do it" task
intact). The notification auto-switch path (`MainActivity`'s `else` branch) was not separately exercised via a
real push, but the same `SelectedProjectStore` write it uses was just proven correct end-to-end via the join flow.

Logic-level coverage exists via `ProjectCurrentProjectProviderTest` (fallback resolution, 4 cases; 2 more added for
`observeCurrentProjectSummary()`, 6 total, all passing).

## Out of scope for Phase 5b

Phase 6 (data export, delete-account, plan & limits), any change to how a project is created (still zero-projects-
gated) or to `CreateProjectRoute`'s reachability.

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
