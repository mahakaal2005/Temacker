# 2026-09-15 (session 4) — Fix audit bug #2: Google sign-in cancel showed a spurious error

## Context
Continuation of today's earlier sessions. Per the 4-module code audit
(`specs/logs/2026-09-15-manage-roles-reassign-fix-and-full-audit.md`), bug #1 (updateRole
`isLeader` corruption) was already fixed this session. This session fixed bug #2.

## Bug
`FirebaseAuthRemoteDataSource.signInWithGoogle()`'s `GetCredentialCancellationException` catch
block's own comment said "user dismissed the picker — not an error, just stop," but the code
still returned `Result.Error(DataError.Network.UNKNOWN)`, which `LoginViewModel.googleSignIn()`
mapped straight into `state.error` via `.toUiText()` — so a plain cancel showed the user a visible
"something went wrong" error.

## Decision (confirmed with user before implementing)
`Result<D, E>` is strictly `Success`/`Error` — there's no built-in third state to represent
"not a real failure, don't show anything." Two options were considered:
1. **Add `DataError.Network.CANCELLED`**, have `LoginViewModel` special-case it as a no-op in its
   existing `onFailure` lambda. Minimal diff, no signature changes anywhere, stays inside the
   existing `Result`/`DataError` pattern.
2. **Introduce a feature-specific `AuthError` type** replacing `DataError` across
   `AuthRepository`/use cases/`LoginViewModel`, distinguishing `Cancelled` from real failures.
   Bigger blast radius, and overlaps with audit bug #3 ("all auth failures collapse to one generic
   error"), which is explicitly out of scope for this pass per CLAUDE.md rule 14.

User confirmed option 1.

## Fix
- `core/domain/util/DataError.kt`: added `CANCELLED` to `DataError.Network`.
- `FirebaseAuthRemoteDataSource.signInWithGoogle()`: `GetCredentialCancellationException` catch
  block now returns `Result.Error(DataError.Network.CANCELLED)` instead of `.UNKNOWN`.
- `LoginViewModel.googleSignIn()`: `onFailure` now checks `if (error != DataError.Network.CANCELLED)`
  before updating `state.error` — a dismissed picker is now a silent no-op (loading still clears).
- `DataErrorToUiText.kt` left unchanged; `CANCELLED` falls through its existing `else` branch
  (generic "unknown" string) as a safety net, but `LoginViewModel` intercepts it before that
  mapping is ever shown to the user.
- `./gradlew assembleDebug lintDebug` both pass clean.

## Scope
Per CLAUDE.md rule 14, no other audit items (bugs #3/#4, the 4 inconsistencies, or minor/style
items) were touched in this pass.
