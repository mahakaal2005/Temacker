# 2026-09-17 (session 7) — Fix audit item #9 (1/4): ProjectGateScreen Root/Screen split + preview

## Context
Continuation of the one-by-one audit cleanup. Item #9 covers missing/partial `@Preview` coverage
across four screens; each is being landed as its own commit. This one:
"`ProjectGateScreen` has none at all (and also has no Root/Screen split per
android-presentation-mvi's convention — the UI is inlined directly in `ProjectGateRoot`)."

## Fix
- `ProjectGateScreen.kt`: extracted the loading `Surface`/`Column`/`CircularProgressIndicator` body
  out of `ProjectGateRoot` into a new stateless `ProjectGateScreen()` composable (no parameters —
  `ProjectGateViewModel` has no `State`, only navigation `Event`s, so there's nothing to pass).
  `ProjectGateRoot` now just observes events and calls `ProjectGateScreen()`.
- Added `ProjectGateScreenPreview` (`@Preview(showBackground = true)`).

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Per CLAUDE.md rule 14, no other item #9 screens were touched in this commit — RosterScreen,
ManageRolesScreen, and HomeScreen's remaining preview gaps land as separate commits.
