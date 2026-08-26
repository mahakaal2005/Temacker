# Phase 4 — The Truth About the Team

Status: not started

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

- Load/Stuck/Pulse aggregations against fixture task/handoff/event data.
- Succession flow does not allow reassigning the Leader role directly; confirms roster carries over to the new project cycle.

## Out of scope for Phase 4

Onboarding polish (Phase 5), export/plan screens (Phase 6).

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
