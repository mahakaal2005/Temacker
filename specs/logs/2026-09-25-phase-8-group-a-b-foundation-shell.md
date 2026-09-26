# 2026-09-25 — Phase 8 Groups A & B: design tokens, floating nav bar, real project switcher

## Context
UI/UX redesign (Phase 8, spec in `specs/office/phase-8-ui-redesign.md`). User's top complaint was that the
project switcher on Board was invisible — this session's Group B fixes that directly. No ViewModel, State,
Action, Event, use case, repository, Room, Firestore or navigation route was touched.

## Changed

### Group A — foundation (`core/presentation/designsystem/`)
- New `Spacing.kt` — a 4pt scale (xxs 4 · xs 8 · s 12 · m 16 · l 20 · xl 24 · xxl 32).
- New `Motion.kt` — `spatialDefault()`, `spatialExpressive()` (hero moments only), `effectsSpring()`,
  `rememberReducedMotion()` (reads `Settings.Global.ANIMATOR_DURATION_SCALE`).
- New `Haptics.kt` — `AppHaptics`/`rememberAppHaptics()` wrapping `HapticFeedbackType.Confirm/Reject/
  SegmentTick/ToggleOn/ToggleOff`. This is the only place a haptic type gets chosen (spec §5).
- New `Elevation.kt` — `cardElevation()`/`floatingElevation()`, navy-tinted shadows per the spec's `--e1`/`--e3`.
- `Type.kt` — added the 6 roles screens already called but that fell back to M3 defaults (`headlineSmall`,
  `titleSmall`, `bodySmall`, `labelLarge`, `labelMedium`, `displayLarge`/`displaySmall`). `MonoFontFamily` now
  points at a bundled Roboto Mono file instead of the platform monospace fallback.
- `Color.kt` — added `AppColors` (semantic `onSurfaceMuted`/`success`/`warning`/`danger` groupings).
- `res/font/roboto_mono_medium.ttf` — the real Google Fonts Roboto Mono (Medium), downloaded from
  fonts.gstatic.com. No Gradle dependency added.

### Group B — shell
- New `TmkBottomBar.kt` — a floating pill bottom bar (64dp, 12dp margins, `e3` shadow) replacing the
  full-width Material `NavigationBar`. Real icons (Material Symbols outlined/rounded, already in the
  `material-icons-extended` dependency) instead of the old "B/I/T/Y" letter glyphs. The amber selection pill
  slides between tabs (`spatialExpressive`, snaps under reduced motion), the icon crossfades outline→filled,
  and each tab change fires a `SegmentTick` haptic. `AppDestination`/`LocalInboxBadgeCount` moved here from
  `AppScaffold.kt` (same package, so no caller changes needed there).
- `AppScaffold.kt` rewritten: the bar now floats over content instead of occupying a `Scaffold` bottomBar
  slot, so screens control their own bottom inset via the new `bottomBarContentPadding()` helper.
- `SwitcherPill.kt` rebuilt as the fix for the actual complaint: it's now a real `Surface` pill with a
  border and an **always-visible** chevron (it used to only show once you already had a 2nd project — the
  root cause of "I can't see how to switch"). `projectName`/`metaLine` are now nullable and render a
  skeleton bar instead of disappearing while the project is still loading. Added an optional `trailing` slot
  for a per-tab contextual action.
- Removed the duplicate per-tab `TopAppBar` ("Board"/"Inbox"/"Team"/"Profile") on all 4 tab screens —
  `BoardScreen.kt`, `InboxScreen.kt`, `TeamScreen.kt`, `ProfileScreen.kt`. That was ~64dp of wasted space
  and it visually buried the switcher underneath it. Team's "Start new cycle" action moved from that bar
  into the new `SwitcherPill` trailing slot (still Leader-gated, still has a real `contentDescription`).

## Deviations from the spec HTML (both user-approved 2026-09-25 per the AskUserQuestion in this session)
- Bottom nav is a floating pill, not the spec's full-width `.navbar`.
- `MonoFontFamily` is bundled Roboto Mono, not the platform monospace it silently fell back to before.

## Verified
**Nothing.** This cloud container has no Android SDK, so `./gradlew :app:assembleDebug :app:lintDebug
:app:testDebugUnitTest` could not be run (CLAUDE.md rule 12). I did a manual read-through of every touched
file for import correctness, unused symbols, and nullable-type propagation, and fixed 2 real mistakes that
turned up that way (a missing `Modifier.width` import in `TmkBottomBar.kt`, a stray `@OptIn
(ExperimentalMaterial3Api::class)` left over on `BoardScreen.kt` after its only experimental-API use
(`TopAppBar`) was deleted). That is not a substitute for a real build — the user needs to run the Gradle
build/lint/test command on their own machine before this is "done" per rule 12.

## Not done yet
- Groups C–F of the Phase 8 spec (shared components, Board/Task-detail/Handoff/Incoming, Team/Profile/Auth
  sweep, notification visuals) — not started.
- Device verification (font scale, TalkBack, reduced motion, gesture nav, keyboard-open) — needs a device.
- The Board FAB restyle (extended FAB that shrinks on scroll) mentioned in the spec's §4 — not done this
  session; still the plain `FloatingActionButton` from before.
