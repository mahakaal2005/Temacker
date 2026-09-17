# 2026-09-17 (session 7) — Fix audit minor #10: password-visibility state ownership

## Context
Continuation of the one-by-one audit cleanup. Item #10, originally stated: "Password-visibility
toggle in `LoginScreen.kt` uses local `remember` instead of `LoginState`/`LoginAction`, against
android-compose-ui's state-ownership rule (its only exception list is framework-owned state like
`LazyListState`)."

## Fix
- `LoginState.kt`: added `isPasswordVisible: Boolean = false`.
- `LoginAction.kt`: added `OnTogglePasswordVisibility`.
- `LoginViewModel.kt`: handles `OnTogglePasswordVisibility` by flipping `isPasswordVisible` in state.
- `LoginScreen.kt`: removed the local `var isPasswordVisible by remember { mutableStateOf(false) }`
  and its now-unused `remember`/`mutableStateOf`/`setValue` imports; the password field's
  `visualTransformation` and trailing "Show"/"Hide" button now read/drive `state.isPasswordVisible`
  via `onAction`.

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Per CLAUDE.md rule 14, no other audit items were touched. No new `@Preview` was added for the
password-visible state — preview coverage is item #9's scope, not this one.
