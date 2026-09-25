# 2026-09-25 — Phase 8: fix CodeRabbit's 5 findings on PR #1

## Context
CodeRabbit reviewed [PR #1](https://github.com/mahakaal2005/Temacker/pull/1) (Phase 8 Groups A & B) after
the user manually triggered it, and found 5 real Major-severity bugs — all self-inflicted regressions from
this session's own changes. None were optional/nit-level, so per the PR-babysitting posture they were bug
reports to verify and fix, not deferrable. Plan approved via AskUserQuestion before implementing.

## Fixed (commit cf74268)
1. **Snackbar hidden behind the floating bar** — `AppScaffold.kt`'s `snackbarHost` slot is now wrapped in
   `Modifier.padding(bottom = TmkBottomBarReservedHeight)`, since the bar no longer lives in Scaffold's own
   `bottomBar` slot for Scaffold to automatically reserve space for.
2. **Content/FAB could end up under the bar on a larger nav-bar inset** —
   `TmkBottomBar.kt`'s `bottomBarContentPadding` now also adds `padding.calculateBottomPadding()`, exactly
   CodeRabbit's suggested diff.
3. **Selected tab wasn't announced to TalkBack** — added `this.selected = isSelected` inside the existing
   `.semantics {}` block (disambiguated with `this.` since `TmkBottomBar`'s own `selected: AppDestination`
   parameter shares the name — a real shadowing risk caught while implementing, not just applying the diff
   verbatim).
4. **Profile's Sign out / Leave project could become unreachable** — content `Column` now wrapped in
   `.verticalScroll(rememberScrollState())`. This required replacing `Spacer(Modifier.weight(1f))` with a
   fixed `Spacer(Modifier.height(Spacing.xxl))`, since `weight()` inside a scrollable `Column` crashes at
   runtime (unbounded height) — CodeRabbit's finding didn't mention this follow-on fix, caught it myself.
5. **Project switcher was disabled while loading** — dropped `enabled = !isLoading` (always tappable now)
   and the chevron's loading-alpha hiding, since hiding the affordance on an always-actionable row would
   have contradicted the fix. Also removed the now-dead `isLoading` local and its now-unused `alpha` import.

## Verified
Manual read-through only — no Android SDK in this container (same limitation as Groups A/B). Replied to and
resolved all 5 GitHub review threads, each naming the fix commit.

## Not done yet
Groups C–F of the Phase 8 spec. Continuing into Group C next per the user's explicit choice.
