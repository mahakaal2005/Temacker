# Phase 5 — v1.0, Used by a Team That Isn't Yours

Status: not started

Onboarding polish for people who join a project someone else built. The screens below need no new data model or nav; the project switcher (open decision below) would.

## Screens / Flows

- **Invited member, first run** — lands after joining via invite-code deep link or the join flow.
- **What a role means** — read-only permission list (marks, not switches), names who granted the role. Reuses Manage Roles' data.

## Open decision: project switcher (added 2026-09-19, not yet agreed)

Today the app treats "current project" as the first project the user belongs to (`CurrentProjectProvider`),
so someone in two projects cannot see the second one anywhere. Phase 3 surfaced it: a push for a project other
than the current one is dropped on tap (the app lands where it was) because no screen can show it.

If agreed, it needs, before any code (CLAUDE.md rule 2):
- A persisted "selected project" and a switcher entry point (You screen or Board header); `CurrentProjectProvider` reads it.
- Notification taps for another project switch to it first, then navigate (the intent already carries `projectId`).
- Architecture-doc changes (nav, the `CurrentProjectProvider` contract) and a phase spec section agreed with the user.
Until then this stays out of scope and the single-project behaviour is unchanged.

## Files (representative)

- `feature_project/presentation/{invited_first_run,role_explainer}/*` (presentation-only additions; no new domain/data).

## Testing

- First-run screen appears once per newly-joined project, not on every app open.
- Role explainer reflects the member's actual permission snapshot and role-granter.

## Out of scope for Phase 5

Export/plan screens (Phase 6). The two screens above add no new data model or nav; the switcher only if the decision above is agreed.

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
