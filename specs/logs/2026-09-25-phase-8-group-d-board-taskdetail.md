# 2026-09-25 — Phase 8 Group D: Board and Task detail

## Context
Continuing Group D of `specs/office/phase-8-ui-redesign.md` after Group C's shared components (see
`specs/logs/2026-09-25-phase-8-group-c-components.md`).

## Board (commit 4dc3e54)
Covered in its own commit message — SegmentedTabs, InfoStrip, richer TaskCard (Avatar, StatusChip,
offered/held meta line), 4 differentiated empty states, animateItem(), a collapsing FAB.

**Risk flagged for the user's build check:** `TaskCard`'s press-scale uses `Card(onClick=..., interactionSource
= interactionSource, ...)`. Compose Foundation has been migrating some components off raw
`MutableInteractionSource` params in recent releases; if this pinned BOM (2026.02.01) already completed
that for `Card`, the param may be deprecated (should still compile) or moved. Worth checking first if this
file fails to build.

## Task detail (this commit)
- **New `core/presentation/components/BatonTrail.kt`** — the spec's "signature component": a `Canvas`-drawn
  vertical connector line runs behind every node. Amber = created, teal = accepted, hollow (stroke-only) =
  still open/offered, coral outline = declined. The newest node pulses once on entry via an `Animatable`
  overshoot (`spatialExpressive` then settling with `spatialDefault`), skipped under reduced motion.
  **Flagged risk:** the connector-to-dot vertical alignment (`dotCenterY = 14.dp`) is a constant, not
  measured against the adjacent text's real first-line position — worth a visual check once a real
  build/preview is available.
- **`TaskDetailScreen.kt` rewritten:**
  - Made scrollable (`verticalScroll`) — a long trail used to push Hand off entirely off-screen.
  - The task's description and due date are now shown (previously never rendered anywhere on this screen).
  - "Mark done" moved out of the ⋮ overflow menu into a pinned button next to "Hand off" (both `TmkButton`) —
    it's a primary action for the holder, not a secondary one.
  - Delete confirmation uses the shared `ConfirmDialog`.
  - Back arrow is now `Icons.AutoMirrored.Filled.ArrowBack` (was the non-mirrored `Icons.Default.ArrowBack`).
  - The trail is built from the existing `Handoff`/`Task` fields (`offeredAt`, `respondedAt`, `createdAt`) by
    a new pure `trailNodesFor()` mapper in this file — no ViewModel/State change. "Held N days" for an
    accepted entry is computed against the next-more-recent entry's start time (or now, for the current
    holder), which is the closest a UI-only mapper can get without a new backing field.

No ViewModel/State/Action/Event, use case, repository, Room, Firestore or navigation route changed.

## Verified
Manual read-through only (imports, unused-symbol check, confirmed every referenced `TaskDetailAction`/state
field already exists unchanged) — still no Android SDK in this container.

## Not done yet
Hand off (bottom sheet), Incoming (hero moment), Decline, New task, Queue — the rest of Group D.
