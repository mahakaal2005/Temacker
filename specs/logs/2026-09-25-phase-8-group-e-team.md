# 2026-09-25 — Phase 8 Group E: Team (SegmentedTabs, Roster, Load, Stuck, Pulse)

## Team shell
- `TabRow`/`Tab` replaced with the shared `SegmentedTabs` (sliding pill track, matches Board's tab style).
- Tab selection moved from `remember` to `rememberSaveable` — was resetting on rotation.
- "Start new cycle" moved from a bare, unlabeled `AutoAwesome` icon (one of the original audit's top
  complaints — nobody could guess what it did) into a proper overflow: a `MoreVert` icon opens a
  `DropdownMenu` with a labelled "Start new cycle" item, still Leader-gated and still in the header's
  trailing slot.

## Roster
- Header row: "Invite" is now a `FilledTonalButton` with icon + label instead of a bare icon-only
  `IconButton` — it's the primary team action, per the audit.
- "Manage roles" moved out of the header row into a `ListRow` (inside an `InsetGroup`) at the top of the
  member list, with a chevron — matches how every other "go somewhere else" row in the app now looks.
- Member rows: the hand-rolled `take(2)`-initials avatar circle is replaced with the shared `Avatar`
  component (fixes the same wrong-initials bug Group C fixed elsewhere), the role name is now a
  `StatusChip` (amber for Leader, neutral otherwise) instead of plain colored text, and rows without an
  overflow menu (i.e. the row is only navigable, not actionable) now show a chevron hint so it's clear
  tapping does something.
- "Reassign role" changed from a plain `AlertDialog` with a clickable text list to a `TmkSheet` with
  `RadioButton` rows — matches the spec's "radio rows" instruction. The invite-code sheet was also moved
  from a raw `ModalBottomSheet` onto `TmkSheet` for the same rounded-corner treatment as the rest of the
  app (not mandated by spec section 9 specifically, but consistent with §3/§4's "one sheet shape").

## Load
- Empty state uses the shared `EmptyState` component instead of a single grey line.
- Each holder row gets a thin horizontal bar (`Canvas`, `NeutralWash` track + primary-color fill) showing
  their active-task count relative to the busiest holder on the team — there's no fixed task cap in the
  domain model, so "share" here is relative to `max(totalActive)`, not a percentage of a hard limit.

## Stuck
- Empty state uses `EmptyState`.
- "Unanswered for Xh" changed from plain amber text to a `StatusChip` — amber under 48h, coral at/after,
  matching the notification factory's own waiting-nudge threshold.

## Pulse
- Empty state uses `EmptyState`.
- Each event gets a small icon badge (add/delete/send/check/cancel/task-complete per `TeamEventType`) on a
  neutral-wash circle.
- Events are now grouped under day headers ("Today" / "Yesterday" / `MMM d, yyyy`), computed purely in the
  Composable from the existing `at: Long` timestamp — no ViewModel change. Relies on the existing list
  already being sorted newest-first so same-day events stay contiguous under `groupBy`.

## Verified
Manual read-through only — no Android SDK here. No ViewModel/State/Action/Event changes anywhere in this
batch.

## Next
Manage roles, Role explainer, Succession — then Profile/Your data/Plan & limits/Notification rationale.
