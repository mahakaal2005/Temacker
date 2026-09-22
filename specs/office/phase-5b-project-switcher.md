# Phase 5b — Project Switcher

Status: code complete, partially verified on device (2026-09-22, session 2) — see "On-device verification" below
for what's confirmed vs. still open.

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

**Not yet exercised: the actual multi-project switch (pick a row → Board/Team update → survives relaunch) and the
notification auto-switch.** These need a second real, distinct project to join into, which itself needs a fresh
account with zero projects to create it (an existing account can't create a second project either — same
zero-projects gate on `CreateProjectRoute`). Attempted this on-device by signing in with a different Google
account via the Credential Manager picker; the picker only offers accounts already authorized for this app
(Rudra, FAIQUA) and reaching a third, unused account requires either "More saved sign-ins" (still scoped to
device-saved credentials, not a fresh one) or precise multi-step picker navigation that proved too fragile to
drive reliably via blind `adb input tap` coordinates — one attempt accidentally re-selected Rudra's own account
instead of cancelling. A human tapping the real picker would do this in seconds; left for the user rather than
continuing to guess coordinates. (A separate, now-abandoned attempt to seed a second project via direct Firestore
REST writes confirmed a real, pre-existing limitation: `OfflineFirstProjectRepository.observeUserProjects()`'s
Room-backed emission reads `membershipDao`, which is only ever seeded by the app's own `createProject`/
`joinProject` code — a REST-created member doc never appears in the switcher without a matching local write. Not a
bug introduced here. Test docs were cleaned up immediately after.)

Logic-level coverage exists via `ProjectCurrentProjectProviderTest` (fallback resolution, 4 cases) and the
compiled/previewed switch-project and join-project screens; the actual multi-project tap-through is the one piece
still unverified end-to-end.

## Out of scope for Phase 5b

Phase 6 (data export, delete-account, plan & limits), any change to how a project is created (still zero-projects-
gated) or to `CreateProjectRoute`'s reachability.

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
