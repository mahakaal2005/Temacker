# 2026-09-25 — Phase 8 Group D: Hand off and Incoming

## Context
Continuing Group D after Board and Task detail (see `specs/logs/2026-09-25-phase-8-group-d-board-taskdetail.md`).

## Hand off
- Content rebuilt with the shared components: `ListRow`+`Avatar` for member rows, `TmkButton` for
  Send handoff (now shows a spinner while sending — the old plain-disabled button gave no
  feedback), `EmptyState` for the "you're the only member" dead end (fixes the audit's ❌ finding —
  a blank list with a permanently-disabled button and no explanation).
- Added `imePadding()`/`navigationBarsPadding()` so the keyboard doesn't cover Send.
- **Deviation flagged in the file itself:** the spec calls for Hand off as a modal bottom sheet.
  Doing that properly needs a real overlay/dialog nav destination, which is a navigation-graph
  change outside this phase's presentation-only scope (CLAUDE.md rule 2) — so this keeps the
  existing full-screen destination and only redesigns its content.

## Incoming (the hero moment)
- Avatar → headline → description/due date → note → stats card now enter with a staggered
  fade+rise (40ms stagger, `effectsSpring`/`spatialDefault`), skipped under reduced motion.
- Task description and due date are now shown — previously never rendered on this screen despite
  being on `Task`.
- Accept/Decline fire a `Confirm` haptic the moment the decision is made, before the network
  round-trip — the animation and haptic never block or delay navigation, both fire synchronously
  with `onAction`.
- Both buttons are now `TmkButton`; Accept shows its own loading spinner while `isResponding`.
- The stale-offer "unavailable" state uses `EmptyState` with a "Back to Inbox" button — it used to
  be plain text with no way out except the back arrow (the audit's ⚠️ finding).

**Scope reduction, noted rather than silently skipped:** the spec's more ambitious "baton-arc sweep"
animation on Accept (a `Canvas` path from the sender's avatar to yours) was not built — the stagger,
haptic and loading-state fixes above were judged higher value for the time available, and a custom
path animation carries real risk without a way to visually verify it in this container.

No ViewModel/State/Action/Event, use case, repository, Room, Firestore or navigation route changed.

## Verified
Manual read-through only (imports, confirmed every referenced state/action member is unchanged) — still no
Android SDK here.

## Not done yet
Decline, New task, Queue — the rest of Group D.
