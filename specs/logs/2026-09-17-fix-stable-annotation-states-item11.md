# 2026-09-17 (session 7) — Fix audit minor #11: missing @Stable on states

## Context
Continuation of the one-by-one audit cleanup. Item #11, originally stated: "`LoginState` contains an
unstable `UiText` (sealed interface) field with no `@Stable` annotation, per android-compose-ui's
stability rules."

## Investigation
Checked every presentation `State` class for the same gap (a `UiText?`, `List<T>`, `Map`, or `Set`
field with no `@Stable`). Found the same gap in `HomeState`, `ProfileState`, `CreateProjectState`,
`JoinProjectState` (`UiText?` is their only unstable field, same case as `LoginState`), and
`RosterState`/`ManageRolesState` (already unstable via `List<Membership>`/`List<Role>`, also missing
`@Stable`). Presented this to the user — **chose to fix all 7 states** rather than just the one the
audit item literally named.

## Fix
Added `@Stable` (from `androidx.compose.runtime.Stable`) to: `LoginState`, `HomeState`,
`ProfileState`, `CreateProjectState`, `JoinProjectState`, `RosterState`, `ManageRolesState`.

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Per CLAUDE.md rule 14, no other audit items were touched. `ProjectGateViewModel` has no `State`
class (no screen), so nothing to annotate there.
