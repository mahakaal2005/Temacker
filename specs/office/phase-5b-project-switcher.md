# Phase 5b — Project Switcher

Status: code complete, partially verified on device (2026-09-22) — see "On-device verification" below for what's
confirmed vs. still open.

Closes the gap Phase 5 deliberately left open: "current project" is the first project the user belongs to
(`CurrentProjectProvider`), so someone in two projects can't see the second, and a push for a non-current
project is dropped on tap. One new screen plus real selection state.

## Screens / Flows

- **Switch project** — list of the user's projects (name, member count, the viewer's role in that project, e.g.
  "8 members · Leader" per the concept HTML's `switcher-meta` line), current selection marked, tap another row to
  select and pop back. Opened from a new app-bar header.
- **App bar switcher pill** — project name + meta line, chevron only when the user belongs to 2+ projects; no
  chevron/tap target for a single-project user.
- **Entry point shipped on Team and You only, not Board/Inbox.** Team (`RosterState`) and Profile (`ProfileState`)
  both already assemble project name + member count + role within their own feature (`feature_project`,
  `feature_profile`); `feature_tasks` (Board/Inbox) has no cross-feature contract exposing a project's *name* today
  (only its id, via `CurrentProjectProvider`), and adding one was out of this pass's approved scope. Switching is
  still fully available (via Team or You), and Board/Inbox correctly follow whatever's selected — they just don't
  carry their own entry point yet. Flagged as follow-up, not silently dropped.

## Agreed decisions (2026-09-22)

- **Entry point: `AppScaffold` top bar.** New optional `header` slot, default `null` so every existing call site
  keeps compiling until updated.
- **Notification tap for a non-current project: auto-switch then navigate.** Persist that project as selected,
  then open the route — replaces today's silent drop in `MainActivity.kt`.
- **Single-project users: hide the switcher affordance.** No chevron, no tap target, when `ObserveUserProjectsUseCase`
  returns exactly one project.
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
- **Found and fixed a real bug during this pass:** `SwitcherPill` sat under the status bar, overlapping the
  clock/battery icons — it's a plain composable placed in `Scaffold`'s `topBar` slot, which (unlike `TopAppBar`)
  doesn't consume the status-bar inset on its own. Fixed with `Modifier.windowInsetsPadding(WindowInsets.statusBars)`
  on the pill's `Surface`. Re-verified clean on device after the fix.
- Single-project state (the account's real, current state) correctly shows no chevron and no tap target.

**Not exercised on device: the actual multi-project switch (tap chevron → pick a row → Board/Team update →
survives relaunch) and the notification auto-switch.** Attempted to seed a second project for this account to test
against: a project/role/member doc created directly via the Firestore REST API (bypassing the app's own
create/join code) is correctly picked up into `projectDao` by the existing `observeUserProjects()` listener, but
never appears in the switcher because `membershipDao` (the table `observeUserProjects()`'s Room-backed emission
actually reads) is only ever seeded by the app's own `createProject`/`joinProject` code paths, which explicitly
upsert the new membership row — a REST-created member doc has no such client-side write to trigger it. This is a
pre-existing characteristic of the offline-first sync design (see `OfflineFirstProjectRepository`'s code comment),
not a bug introduced here. Tried to route around it by editing the local Room DB file directly and pushing it back
to the device; that action was blocked by the auto-mode classifier as a destructive local-data overwrite, and the
attempt was abandoned rather than worked around. The Firestore test docs were deleted and the device was confirmed
back to its clean, pre-test state (screenshots, no active call).

The real multi-project flow needs either a second real project (the app has no "create another project while
already in one" entry point yet — expected, since the switcher is what would eventually enable that) or the
device DB edit done with the user's explicit permission. Logic-level coverage exists via
`ProjectCurrentProjectProviderTest` (fallback resolution, 4 cases) and the compiled/previewed switch-project
screen; the end-to-end tap-through is the one piece still unverified.

## Out of scope for Phase 5b

Phase 6 (data export, delete-account, plan & limits), a create/join entry point inside the switcher screen itself,
any change to how a project is created or joined.

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
