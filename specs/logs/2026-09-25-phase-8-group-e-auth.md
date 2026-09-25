# 2026-09-25 — Phase 8 Group E: Auth (Splash, Login)

## Context
First slice of Group E (Team, Project, Profile, Auth and onboarding screens), per spec §9.

## Splash
Mark scales 0.92→1 with a fade using `spatialExpressive()` (the one hero moment, per spec), skipped under
reduced motion. "Checking your session…" now appears only after a 600ms `AnimatedVisibility` delay instead
of being visible immediately — avoids a flash on a fast, already-cached session check. Navy background kept.

## Login
- Now scrollable (`verticalScroll` + `imePadding` + `navigationBarsPadding`) — previously the keyboard could
  cover the fields/buttons entirely.
- Brand block (mark, title, subtitle) fades in once on entry with a short stagger (subtitle 90ms behind).
- Google moved to the first/primary option, email/password second, per spec ("Google is the primary route").
- Removed the "No password to remember" line, which directly contradicted the email/password form sitting
  right above it.
- Email field gets IME `Next` → focuses password; password field gets IME `Done` → submits, via a
  `FocusRequester`.
- The error now renders as an inline banner (icon + text, `AppColors.dangerWash`/`onDanger`) directly under
  the brand block instead of plain red text after the "no password" line — visible without scrolling past the
  keyboard.

**Caught during self-review:** the brand Column originally kept its `Modifier.weight(1f)` from the
non-scrolling layout to center it vertically. Inside the now-scrollable parent `Column`, `weight()` gets an
unbounded (infinite) max-height constraint and crashes at runtime — the same failure mode as the
`Spacer(weight(1f))` bug CodeRabbit found in `ProfileScreen.kt` during Group A/B. Fixed by dropping the
weight and using fixed top padding instead, before it ever reached a build (still can't build-verify this
container).

## Verified
Manual read-through only — no Android SDK here. No ViewModel/State/Action/Event change — `LoginAction`,
`LoginState`, `SplashViewModel` all untouched.

## Next
Continue Group E: No project, Create project, Join project, Invited first run.
