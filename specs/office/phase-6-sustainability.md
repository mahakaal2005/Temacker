# Phase 6 — Sustainability

Status: complete and fully device-verified (2026-09-23) — see
`specs/logs/2026-09-23-phase-6-your-data-plan-limits.md`. Firestore rules change for the self-delete
membership branch passes `npm run test:rules` (93/93) and is deployed to `temacker-a0252`. Export
(JSON + CSV), both screens' live counts, and delete-account (both the Leader-blocked path and, with
explicit user authorization on a real non-Leader test account, the actual success path) are all
device-verified on RZCWA28EAZF. One real bug found and fixed during that testing: a stale Firestore
listener raced the delete-account transition and briefly surfaced a wrong permission error with a
stuck "Deleting…" button; fixed with the same `isSigningOut`-style guard `ProfileViewModel` already
uses for sign-out. The fix compiles and is installed but has not been re-verified live (would need a
second real account deletion).

Data ownership and the money question, answered with static screens (no billing integration).

## Screens / Flows

- **Your data** — export (JSON/CSV of the user's own data) and delete-account. Delete-account is one of only 3 places in the whole app allowed to use the coral/destructive color.
- **Plan & limits** — static: free tier for a single team, org-tier messaging.

## Placement

Default: extend `feature_profile`. Only split into a new `feature_settings` if it grows large enough
to warrant it (architecture §1 — don't create a package prematurely).

## Files (representative)

- `feature_profile/presentation/{data_export,delete_account,plan_limits}/*`
- Export use case reading the user's own Firestore/Room data; delete-account use case + confirmation flow.

## Testing

- Export produces the user's own data only (no cross-user leakage).
- Delete-account requires confirmation and actually removes the account/session.

## Out of scope for Phase 6

None — last phase in the roadmap.

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
