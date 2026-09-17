# 2026-09-17 (session 7) — Fix audit minor #12: style cleanup

## Context
Continuation of the one-by-one audit cleanup. Item #12, originally stated: "Unused `Stone`
design-system color token; two comment blocks (`CoreModule.kt`, `CurrentActivityHolder.kt`) exceed
the one-line comment rule."

## Fix
- `core/presentation/designsystem/Color.kt`: removed the unused `Stone` token (confirmed no
  references anywhere in `app/src/main/java`).
- `core/di/CoreModule.kt`: condensed the 3-line comment above the `CurrentActivityHolder` binding
  into one line, keeping the WHY (`createdAtStart` timing requirement).
- `core/data/activity/CurrentActivityHolder.kt`: condensed the 3-line class-header comment into one
  line, keeping the WHY (Credential Manager needs an Activity context).

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Per CLAUDE.md rule 14, no other files were touched.
