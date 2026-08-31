# Progress

Read this first, every session, before touching code — it's the current status snapshot. Full task
detail lives in each phase's spec file (`specs/office/phase-N-*.md`); this file only tracks
done/left. Update it after every session or completed task.

**Current phase: 1 — Auth, projects, roles.**

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
- [ ] **Not yet verified on-device** — no emulator/device was attached this session (`adb devices` empty). Build and static wiring are correct; the actual golden-path click-through (sign in → create/join project → roster → manage roles → profile → sign out) has not been run. Do this before marking Phase 1 screens complete.
- [ ] Unit tests (ViewModel tests per phase-1 spec's Testing section — create/join project, permission toggles, Leader immutability) — not started.
- [x] Firestore security rules written (`firestore.rules`, repo root) for projects/roles/members/inviteCodes, matching the actual field shapes in `feature_project/data/mapper/*Mapper.kt` and the exact reads/writes in `feature_project/data/remote/*RemoteDataSource.kt`. Enforces: read access gated on project membership; role/member creation validated against `RolePermissions.ALL_GRANTED`/`NONE`; role reassignment and invite-code management gated on the matching `RolePermissions` flag; the Leader role/membership can never be edited, deleted, or reassigned. **Known accepted gap:** `createProjectWithLeader()`'s project+role+member `WriteBatch` is evaluated by Firestore against pre-batch state per write, so true cross-document integrity isn't enforceable there without a Cloud Function (Admin SDK) — the rules instead gate on "project doc doesn't exist yet," which limits a malicious signed-in user to spamming extra docs inside their own brand-new project, nothing more. **Not yet done:** rules haven't been deployed (no `firebase.json`/Firebase CLI project set up in this repo) or tested against the Firestore emulator — do both before trusting them in production.

## Phase 2 — Board and the baton (`phase-2-board-baton.md`)
- [ ] Rewrite architecture doc's Data Model + Phase Roadmap to holder/handoff model
- [ ] feature_tasks scaffold + domain (Task, Handoff, use cases)
- [ ] feature_tasks data layer (Firestore + Room + OfflineFirstTaskRepository)
- [ ] Screens: Board, Empty board, New task, Task detail/trail, Hand-off, Incoming, Decline, Read-only board, Delete confirm
- [ ] Bottom nav shell (Board · Team · You)
- [ ] Permission gating on board actions
- [ ] Firestore security rules for handoffs/events

## Phase 3 — Notifications, honest offline (`phase-3-notifications-offline.md`)
- [ ] FCM setup + permission rationale screen
- [ ] Lock-screen notification style
- [ ] Inbox nav destination + badge
- [ ] Offline queue screen (WorkManager)

## Phase 4 — Truth about the team (`phase-4-team-truth.md`)
- [ ] Load / Stuck / Pulse tabs inside Team
- [ ] Aggregation use cases
- [ ] Succession (end-of-cycle) flow

## Phase 5 — v1.0, used by others (`phase-5-v1-others.md`)
- [ ] Invited-member first-run screen
- [ ] Role explainer screen

## Phase 6 — Sustainability (`phase-6-sustainability.md`)
- [ ] Your data (export + delete-account)
- [ ] Plan & limits screen

## Open decisions
- Phase 6: extend `feature_profile` vs. new `feature_settings` — default is extend, revisit if it grows.
