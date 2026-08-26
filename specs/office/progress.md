# Progress

Read this first, every session, before touching code — it's the current status snapshot. Full task
detail lives in each phase's spec file (`specs/office/phase-N-*.md`); this file only tracks
done/left. Update it after every session or completed task.

**Current phase: 1 — Auth, projects, roles.**

## Phase 1 — Auth, projects, roles (`phase-1-auth-projects-roles.md`)
- [x] Core layer: `Result`/`DataError` (core/domain/util), `SessionManager` interface + DataStore impl, `UiText`/`ObserveAsEvents` (core/presentation/util), `CoreModule` Koin wiring, `App.kt` + `startKoin`. `AppDatabase` deferred until the first Room entity exists (Room rejects `@Database` with zero entities).
- [ ] feature_auth scaffold (Google Sign-In, AuthRepository, SessionManager)
- [ ] feature_project scaffold (Project, Role, Membership, InviteCode)
- [ ] Screens: Splash, Login, No-project, Create project, Join project, Home stub, Roster, Manage roles, Profile
- [ ] Firestore security rules (projects/roles/memberships/invite codes)

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
