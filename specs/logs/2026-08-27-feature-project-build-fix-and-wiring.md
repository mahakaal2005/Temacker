# 2026-08-27 — feature_project: recover from API-limit cutoff, fix build, wire it up

## Context

Previous session hit the API limit mid-work. A large amount of `feature_project` code
(domain, data, DI module, 6 screens) plus a `feature_profile` ViewModel skeleton had
landed on disk uncommitted, but `specs/office/progress.md` was never updated to reflect
it, the project didn't compile, and none of it was wired into the app (no Koin
registration, no nav graph, `MainActivity.kt` still pointed at the old placeholder).

## What was done

1. **Compile-error pass.** `./gradlew compileDebugKotlin` failed with:
   - Missing `androidx.compose.runtime.getValue` imports in `CreateProjectScreen`,
     `HomeScreen`, `JoinProjectScreen`, `ManageRolesScreen` (delegate resolution error on
     `by state`).
   - Missing `@OptIn(ExperimentalMaterial3Api::class)` on 6 screens using `TopAppBar` /
     `ModalBottomSheet` (`NoProjectScreen`, `CreateProjectScreen`, `HomeScreen`,
     `JoinProjectScreen`, `ManageRolesScreen`, `RosterScreen` + its `InviteCodeSheet`).
   - `Icons.Default.PersonAdd` unresolved in `RosterScreen` — the icon isn't in
     `material-icons-core`. Added `androidx-compose-material-icons-extended` to
     `gradle/libs.versions.toml` and `app/build.gradle.kts`.
   - `./gradlew assembleDebug` now passes clean.

2. **Built the missing `ProfileScreen.kt`.** Only the ViewModel/State/Action/Event
   existed. Built the Root/Screen composable to match the UI spec's P1·09 layout (avatar
   with initials, name/email, current-project/role/member-since card, coral-outlined
   sign-out button, `@Preview`s for loaded + loading states). Added `feature_profile`'s
   `ProfileModule.kt` (Koin) — didn't exist either.

3. **Wired everything up:**
   - `App.kt`: added `projectModule` and `profileModule` to `startKoin`.
   - Built `feature_project`'s nav graph (`ProjectRoutes.kt` / `ProjectGraph.kt`) and
     `feature_profile`'s (`ProfileRoutes.kt` / `ProfileGraph.kt`), following the
     `authGraph` pattern already in the codebase — cross-feature navigation as callbacks,
     never importing another feature's routes.
   - Added `ProjectGateViewModel`/`ProjectGateEvent`/`ProjectGateScreen` — nothing
     previously decided whether a freshly-logged-in user should land on No-project or
     Home. Every project ViewModel (Home, Roster, ManageRoles) independently reads
     `ObserveUserProjectsUseCase().first()` and picks the user's first project, but that
     doesn't help pick the *initial* screen. The gate is a one-shot spinner screen that
     makes that call once and routes accordingly.
   - `MainActivity.kt`: deleted `AppPlaceholderRoute`/`AppPlaceholderScreen`, replaced
     with a `NavHost` wiring `authGraph` → `ProjectGateRoute` → project screens ↔ profile,
     with sign-out routing back to `LoginRoute` via `popUpTo(0)`.

## Known gaps (left for next session)

- **Not verified on-device.** No emulator/device was attached this session
  (`adb devices` returned empty). Build and static wiring are correct; the actual
  golden-path click-through hasn't run. Do this first next session.
- **No unit tests yet** for the phase-1 ViewModels (create/join project, permission
  toggles, Leader immutability) — spec calls for JUnit5 + Turbine + AssertK against fake
  repositories.
- **No Firestore security rules.** The app currently relies on whatever default rules
  the Firebase project has — a real gap before any real usage, not just a checklist item.
- Google Sign-In's `DEVELOPER_ERROR` (statusCode=10) issue from the previous session is
  still untested — email/password remains the working fallback.

## Files touched

See `git status` — this session touched `App.kt`, `MainActivity.kt`,
`gradle/libs.versions.toml`, `app/build.gradle.kts`, six `feature_project` screen files,
and added `ProfileScreen.kt`, `feature_profile/di/ProfileModule.kt`, the
`feature_project`/`feature_profile` navigation packages, and the `ProjectGate*` files.
