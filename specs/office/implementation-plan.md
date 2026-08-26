# Implementation Plan — Index

Derived from `specs/UI/relay-all-phases-android-concept.html` (30 screens, 6 phases). This is an
index only — each phase's real task list lives in its own spec file. For live status, see
`specs/office/progress.md`.

## Product note

The UI concept ("Relay") uses a **holder + handoff** task model — one person holds a task and
explicitly hands it to someone else, who accepts or declines — not the Backlog/In Progress/Review/
Done status-column model currently in `specs/ultimate_android_architecture.md`. That doc's Data
Model and Phase Roadmap sections need rewriting to match — first task in Phase 2 — before any
`feature_tasks` code is written.

## Screen inventory

| Phase | Spec | Screens | Primary feature package(s) |
|---|---|---|---|
| 1 — Auth, projects, roles | `phase-1-auth-projects-roles.md` | Splash, Login, No project, Create project, Join project, Home, Team roster, Manage roles, Profile | `feature_auth`, `feature_project` |
| 2 — Board and the baton | `phase-2-board-baton.md` | Board, Empty board, New task, Task detail + trail, Hand off sheet, Handoff to you, Decline, Read-only board, Delete task | `feature_tasks` |
| 3 — Notifications, honest offline | `phase-3-notifications-offline.md` | Why notifications, Lock screen, Inbox, Offline queue | `feature_tasks` (Inbox), cross-cutting (FCM, WorkManager) |
| 4 — Truth about the team | `phase-4-team-truth.md` | Load, Stuck, Pulse, Succession | `feature_project` (Team tabs) |
| 5 — v1.0, used by others | `phase-5-v1-others.md` | Invited first run, Role explainer | `feature_auth`, `feature_project` |
| 6 — Sustainability | `phase-6-sustainability.md` | Your data, Plan & limits | `feature_profile` |

Navigation grows by phase: Phase 1 has no bottom nav; Phase 2 adds 3 destinations (Board · Team ·
You); Phase 3 adds a 4th (Inbox); Phase 4 adds tabs *inside* Team rather than a 5th destination;
Phases 5–6 add no new destinations.

## Cross-cutting rules (every phase)

- Root/Screen split + `@Preview(uiMode = UI_MODE_NIGHT_YES)` for every state, per CLAUDE.md UI Requirements and architecture §9/§11.
- Color rules: amber = interaction/state, teal = identity/classification only, coral = destruction only (delete task, delete dialog, delete-account — 3 total appearances; sign-out is NOT coral).
- Each phase's spec is written/confirmed before that phase's implementation starts (CLAUDE.md rule 3).

## Verification

After each phase: build + run, walk the phase's screens against the HTML mock side-by-side, run unit
tests for new use cases/repositories, confirm previews render for every state.
