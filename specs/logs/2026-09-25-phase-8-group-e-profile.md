# 2026-09-25 — Phase 8 Group E: Profile, Your data, Plan & limits, Notification rationale (Group E complete)

## Profile (You)
- Identity header now uses the shared `Avatar` (size 56dp) instead of a hand-rolled `take(2)` circle —
  fixes the same wrong-initials bug here too.
- New "PROJECT" `InsetGroup`: Current project ▸ (opens the switcher, chevron + `ListRow`), Your role (now a
  `StatusChip`), Member since.
- New "SETTINGS" `InsetGroup`: Notifications ▸, Your data ▸, Plan & limits ▸ — all `ListRow`s with a
  chevron. Notifications opens Android's own `ACTION_APP_NOTIFICATION_SETTINGS` screen via an `Intent`
  (system settings, per the spec's "or system settings through an existing route") rather than adding a new
  in-app nav route to the one-shot `NotificationRationaleScreen`, which is reached today only from the
  onboarding gate flow — wiring a new Profile → Rationale nav edge would be a navigation-graph change, out
  of this phase's presentation-only scope; the system Intent needs no such change.
- Sign out changed from a coral `OutlinedButton` to a neutral `TmkButton(TEXT)`. Leave project is now the
  only coral (`TmkButtonVariant.DESTRUCTIVE`) item on the screen, per spec.

## Your data
- "Delete my account and data" moved out of the main flow into a separate coral "DANGER ZONE"
  `InsetGroup`-style card at the bottom of the scrollable content, well below the info row — was previously
  stacked directly above the primary Export action.
- Export stays the pinned primary action (now a `TmkButton` with a `Download` icon and its own loading
  state, outside the scrollable area, same position as before).
- Back arrow switched to `AutoMirrored`.

## Plan & limits
- Each usage meter (`LinearProgressIndicator`) now animates from 0 to its real value once on entry, using
  `effectsSpring()` (the design system's no-overshoot spring for color/value changes) instead of jumping
  straight to its final width.
- Back arrow switched to `AutoMirrored`.

## Notification rationale
- The three capability rows (offer/accepted-declined/unanswered) now fade in with an 80ms stagger via a
  small `StaggerIn` wrapper (skipped under reduced motion), instead of appearing all at once.
- "Turn on notifications" now fires `AppHaptics.confirm()` before dispatching its action.
- Back arrow switched to `AutoMirrored`.

## Verified
Manual read-through only — no Android SDK here. No ViewModel/State/Action/Event or navigation-graph changes
anywhere in this batch.

## Group E — complete
Auth (Splash, Login), onboarding (No project, Create/Join project, Invited first run), Team (shell, Roster,
Load, Stuck, Pulse, Manage roles, Role explainer, Succession), and Profile (You, Your data, Plan & limits,
Notification rationale) are all redesigned. See the 5 preceding Group E logs for per-screen detail.

## Next
Group F — system notification visuals (`HandoffNotificationFactory`, the one file this phase is explicitly
allowed to touch beyond pure Compose UI, visuals only).
