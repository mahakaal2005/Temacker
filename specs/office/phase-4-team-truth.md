# Phase 4 — The Truth About the Team

Status: implemented 2026-09-18, pending on-device verification (see `specs/office/progress.md`).
Built ahead of Phase 3 per explicit user request — see the note this file used to carry about Phase
2/3 data being a prerequisite; accepted since Load/Stuck/Pulse only need Phase 1/2's task/handoff data.

Read-only analytics over Phase 2/3 data, plus end-of-cycle succession. Do not start before Phase 2/3
data exists — these metrics are meaningless without real handoff history.

## Screens / Flows

- **Load** — who's holding what, grouped by holder.
- **Stuck** — batons offered but not accepted past a threshold.
- **Pulse** — project throughput/activity feed.
- **Succession** — end-of-cycle screen. Leader role stays non-transferable directly; modeled as "cycle ends, new project inherits the roster," not a direct role reassignment.

All three of Load/Stuck/Pulse are tabs *inside* the existing Team nav destination — no new bottom-nav
destination.

## Files (representative)

- `feature_project/presentation/team/{load,stuck,pulse,succession}/*`
- Aggregation use cases in `feature_project/domain/use_case/` (or `feature_tasks/domain` if they read task/handoff data — keep dependency direction in mind, see architecture §4).

## Testing

- [ ] Load/Stuck/Pulse aggregations against fixture task/handoff/event data — deferred with the
  rest of unit testing, matching Phase 1/2's precedent (rules tests + preview coverage only).
- [x] Firestore rules tests: archival flip (Leader-only, single-key diff), roster-copy create rules
  for roles/members (Leader-only, custom role shapes, spoofed-leadership denied) — 78/78 passing.
- [ ] Succession flow's Leader-role-immutability and roster-carry-over — covered at the rules layer
  above; no ViewModel/use-case unit test written (see deferral note).
- [ ] On-device golden path (see `specs/office/progress.md` Phase 4 section for the exact checklist)
  — not run this session, no device/emulator was available.

## Out of scope for Phase 4

Onboarding polish (Phase 5), export/plan screens (Phase 6).

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
