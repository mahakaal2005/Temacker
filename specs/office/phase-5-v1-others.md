# Phase 5 — v1.0, Used by a Team That Isn't Yours

Status: not started

Onboarding polish for people who join a project someone else built — no new data model or nav.

## Screens / Flows

- **Invited member, first run** — lands after joining via invite-code deep link or the join flow.
- **What a role means** — read-only permission list (marks, not switches), names who granted the role. Reuses Manage Roles' data.

## Files (representative)

- `feature_project/presentation/{invited_first_run,role_explainer}/*` (presentation-only additions; no new domain/data).

## Testing

- First-run screen appears once per newly-joined project, not on every app open.
- Role explainer reflects the member's actual permission snapshot and role-granter.

## Out of scope for Phase 5

Export/plan screens (Phase 6). No new data model or nav changes at all.

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
