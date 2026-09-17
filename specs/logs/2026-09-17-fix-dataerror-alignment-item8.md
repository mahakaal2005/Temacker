# 2026-09-17 (session 7) — Fix audit inconsistency #8: DataError enum alignment

## Context
Continuation of the one-by-one audit cleanup. Items #1–#7 already fixed. Item #8, originally stated:
"`DataError` enum diverges from the `android-error-handling` skill's canonical set without this
deviation ever being flagged/approved per CLAUDE.md rule 2. Also `DataErrorToUiText.kt` has no
explicit branch for `REQUEST_TIMEOUT`."

## Investigation
Compared the live enum against the skill's canonical `DataError.Network` set. Missing: `BAD_REQUEST`,
`FORBIDDEN`, `NOT_FOUND`, `TOO_MANY_REQUESTS`, `PAYLOAD_TOO_LARGE`, `SERVICE_UNAVAILABLE`. Extra (not
in the skill): `PERMISSION_DENIED` (Firestore's distinct insufficient-permission code) and
`CANCELLED` (our own addition from item #2, for a dismissed Google sign-in picker). Presented three
options — align fully, document the current set as an approved deviation, or add only
`TOO_MANY_REQUESTS` — **user chose full alignment to the skill's canonical set**.

## Fix
- `core/domain/util/DataError.kt`: added the 6 missing cases. Kept `PERMISSION_DENIED`/`CANCELLED`
  with a comment documenting them as deliberate, real-case additions rather than an unflagged
  deviation.
- `feature_auth/data/remote/FirebaseAuthRemoteDataSource.kt`'s `toAuthDataError()`: added
  `FirebaseTooManyRequestsException -> TOO_MANY_REQUESTS` and
  `FirebaseAuthWeakPasswordException -> BAD_REQUEST` (checked before
  `FirebaseAuthInvalidCredentialsException`, since `FirebaseAuthWeakPasswordException` is a subtype
  of it — Kotlin `when`/`is` picks the first match). This closes the "weak-password/too-many-requests
  fall to UNKNOWN" gap flagged in item #3's log.
- `core/presentation/util/DataErrorToUiText.kt`: added explicit branches for `REQUEST_TIMEOUT`,
  `SERIALIZATION`, `TOO_MANY_REQUESTS`, `BAD_REQUEST` — the four newly/previously-unmapped cases the
  app actually produces. The remaining skill cases with no current producer (`FORBIDDEN`, `NOT_FOUND`,
  `PAYLOAD_TOO_LARGE`, `SERVICE_UNAVAILABLE`) are left on the generic `else -> error_unknown` branch,
  same as before — nothing regresses, and a mapping can be added when a real producer exists.
- `res/values/strings.xml`: added `error_request_timeout`, `error_serialization`,
  `error_too_many_requests`, `error_bad_request`.

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Per CLAUDE.md rule 14, no other audit items were touched. `FORBIDDEN`/`NOT_FOUND`/
`PAYLOAD_TOO_LARGE`/`SERVICE_UNAVAILABLE` were added to the enum (for skill-set completeness, per the
user's chosen option) but intentionally not wired to any producer or UI string — nothing in the
codebase throws an exception that maps to them yet.
