# 2026-09-25 — Phase 8 Group E: Manage roles, Role explainer, Succession

## Manage roles
- Each editable role's TEAM/TASKS permission rows are now wrapped in `InsetGroup` cards (was a single flat
  `Card` for the whole role) — matches the spec's "permission groups use InsetGroup cards."
- Every `Switch` toggle now fires `AppHaptics.toggleOn()`/`toggleOff()` via `rememberAppHaptics()`.
- Back/add/lock icons switched from `Icons.Default.*` to `AutoMirrored.Rounded`/`Rounded.*`, matching the
  rest of the redesign's icon set.

## Role explainer
- Its TEAM/TASKS capability groups are now wrapped in `InsetGroup` too, for the same reason and the same
  visual language as Manage roles (it's the read-only mirror of that screen).
- Added an optional `modifier` param to the shared `CapabilityRow` (role_copy package) so it could take the
  `InsetGroup`'s internal row padding — backward compatible default, `InvitedFirstRunScreen`'s existing calls
  are unaffected.
- Back arrow switched to `AutoMirrored`.

## Succession
- Added a stepper header (1 Name → 2 Confirm) above the existing two-step flow — a small dot-and-label pair
  per step with a connecting divider, the active/done step filled with the primary color. Purely a header
  addition; `SuccessionStep`/the two-step logic itself is untouched.
- The destructive confirm button and the "Back" button both moved onto `TmkButton`
  (`TmkButtonVariant.DESTRUCTIVE` / `SECONDARY`) instead of hand-rolled `Button`/`OutlinedButton` with manual
  spinner-swap logic — `TmkButton`'s built-in `isLoading` handles the spinner without the button resizing.
- Back arrow switched to `AutoMirrored`.

## Verified
Manual read-through only — no Android SDK here. No ViewModel/State/Action/Event changes.

## Next
Profile (You), Your data, Plan & limits, Notification rationale — the last slice of Group E, then Group F
(system notification visuals).
