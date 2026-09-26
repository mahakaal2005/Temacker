# 2026-09-25 — Phase 8 Group C: shared components

## Context
Continuing Phase 8 (spec in `specs/office/phase-8-ui-redesign.md`) after fixing CodeRabbit's 5 findings on
Groups A/B (see `specs/logs/2026-09-25-phase-8-coderabbit-fixes.md`). This session builds the shared
component library §3 calls for, so the screen-by-screen sweep in Groups D–F reuses one implementation of
each pattern instead of the 6+ hand-rolled copies the original critique found.

## Added (`core/presentation/components/`)
- **`Avatar.kt`** — one initials-circle avatar instead of 6 hand-rolled copies. Fixes the wrong-initials bug
  from the critique: `initialsOf("Mei-Ling Chow")` now returns `"MC"` (first letter of the first and last
  word), not `"ME"` (`take(2)`). Two tones (`NEUTRAL` = teal, `ACCENT` = amber) consolidate the two meanings
  already used ad hoc across screens (regular member vs. Leader/holder) — no new color scheme invented.
- **`StatusChip.kt`** — icon + word pill (`WARNING`/`DANGER`/`SUCCESS`/`NEUTRAL` tones) so a status is never
  colour-only, covering "For you", "Queued", "Not sent", "2 days late", a due date.
- **`EmptyState.kt`** — icon, title, body, optional primary/secondary CTA. Targets every ⚠️/❌ case from the
  empty-state audit in the approved critique.
- **`InfoStrip.kt`** — a fully-tappable banner row (the old sync/waiting/other-projects strips only made
  their text column tappable), entering/exiting via `AnimatedVisibility` with the shared motion tokens.
- **`LoadingState.kt`** — `LoadingState()` (a centered spinner, replacing the pattern duplicated in ~24
  files) and `SkeletonList()` (shimmering placeholder rows, shimmer disabled under reduced motion).
- **`ListRow.kt`** — one row shape (leading/trailing slots, 64dp minimum touch target) for Inbox, Roster,
  Load, Stuck, Pulse, Queue, Handoff and Switch project; `InsetGroup` names the "rounded card holding
  several rows" pattern already used ad hoc for Profile's settings and Roster's manage-roles entry.
- **`TmkButton.kt`** — `PRIMARY`/`SECONDARY`/`DESTRUCTIVE`/`TEXT` in one component with a built-in loading
  state that swaps the label for a spinner without the button resizing.
- **`SegmentedTabs.kt`** — the spec's pill-track tabs (a white thumb slides between segments) replacing the
  stock Material `TabRow`, reusing the same sliding-pill mechanic as `TmkBottomBar`.
- **`TmkSheet.kt`** — a `ModalBottomSheet` wrapper with the spec's 28dp top radius (`--r-sheet`), for turning
  Hand off into a sheet in Group D.
- **`ConfirmDialog.kt`** — one confirm/cancel dialog shape (28dp radius) for the 6 near-identical
  `AlertDialog`s across Task detail, Roster, Your data, Manage roles and Profile.

Every component has at least one `@Preview` per CLAUDE.md's UI rule, wrapped in `TemackerTheme`.

## Deviations / risks flagged for the user
- `TmkSheet`'s preview directly renders a `ModalBottomSheet`, which is a known-flaky pattern in the Android
  Studio preview renderer depending on the Compose version — flagged in a comment in the file; worth an
  extra look once a real build/preview pass is possible.

## Verified
Manual read-through only (imports, name collisions with existing composables, no duplicate top-level
declarations) — this container still has no Android SDK. None of these components are wired into any screen
yet, so nothing existing was put at risk by this session; Groups D–F will consume them.

## Not done yet
Wiring these components into Board, Task detail, Hand off, Incoming, Decline, New task, Queue (Group D), the
rest of the screen sweep (Group E), and notification visuals (Group F).
