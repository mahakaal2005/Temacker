# 2026-09-15 (session 5) — Fix audit bug #3: auth failures collapsed to one generic error

## Bug
Every failure path in `signInWithGoogle`/`signInWithEmail`/`registerWithEmail`
(`FirebaseAuthRemoteDataSource.kt`) mapped to `DataError.Network.UNKNOWN`, even though more
specific `DataError` cases already existed and went unused. Wrong password, a duplicate email on
registration, and no internet all rendered the same generic error message to the user.

## Fix
Added a private `Exception.toAuthDataError()` mapper using Firebase Auth's exception hierarchy,
used in place of the hardcoded `DataError.Network.UNKNOWN` in the generic `catch (e: Exception)`
blocks of all three sign-in paths:
- `FirebaseNetworkException` → `NO_INTERNET`
- `FirebaseAuthUserCollisionException` (duplicate email/credential) → `CONFLICT`
- `FirebaseAuthInvalidCredentialsException` / `FirebaseAuthInvalidUserException` (wrong password,
  malformed/expired credential, no such user) → `UNAUTHORIZED`
- Everything else → `UNKNOWN` (unchanged fallback)

Also changed `GoogleIdTokenParsingException`'s catch block from `UNKNOWN` to `SERIALIZATION`,
since that's exactly what it is (a malformed token failing to parse).

## Known gap (left for audit item #8)
`FirebaseAuthWeakPasswordException` and `FirebaseAuthTooManyRequestsException` have no matching
`DataError.Network` case in the current enum (no `BAD_REQUEST`/`TOO_MANY_REQUESTS`), so they still
fall through to `UNKNOWN`. Not fixed here — adding new `DataError` cases is explicitly item #8's
scope (aligning the enum against the `android-error-handling` skill's canonical set), not this
item's.

## Build
`./gradlew assembleDebug lintDebug` both pass clean.
