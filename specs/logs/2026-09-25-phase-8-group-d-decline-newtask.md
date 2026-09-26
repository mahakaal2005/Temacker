# 2026-09-25 — Phase 8 Group D: Decline and New task

## Context
Finishing Group D after Board, Task detail, Hand off and Incoming.

## Decline
- Made scrollable + `imePadding()`/`navigationBarsPadding()` (previously the keyboard could cover
  the field and button).
- `TmkButton` replaces the plain `Button` — shows a spinner while sending.
- Back arrow is now `AutoMirrored`.
- **Deviation flagged in the file:** the critique wanted "which task you're declining" shown on
  this screen. `DeclineState` has no task title field, and adding one is a State change outside
  this presentation-only phase — noted rather than done silently. The "does a chip append or
  replace the text" question from the critique is `DeclineViewModel` behavior, also untouched.

## New task
- Made scrollable + `imePadding()`/`navigationBarsPadding()`.
- The date field now opens the picker on a tap anywhere on it, not just the trailing icon (wrapped
  in a `clickable` `Box`).
- The Details field grows with content (`minLines = 3`) instead of a fixed 100dp box.
- `TmkButton` replaces the old pattern where a spinner stacked above a still-visible "Create task"
  label — now the spinner replaces the label in place.

No ViewModel/State/Action/Event, use case, repository, Room, Firestore or navigation route changed in
either screen.

## Verified
Manual read-through only (imports, no unused symbols left, confirmed referenced state/action members are
unchanged) — still no Android SDK here.

## Not done yet
Queue — the last screen in Group D.
