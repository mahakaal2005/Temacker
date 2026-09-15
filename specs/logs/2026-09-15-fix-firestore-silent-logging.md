# 2026-09-15 (session 5) — Fix audit bug #4: Firestore exceptions/listener errors logged nowhere

## Bug
`SafeFirestoreCall.kt`'s generic `catch (e: Exception)` branch and both `FirestoreFlow.kt`
`snapshots()` extensions' `close(error)` calls had no `Log.e`/stack trace at all — a Firestore
write or listener failure left zero trace in logcat, making on-device debugging (this audit's
whole session 3 detour) far harder than it needed to be.

## Fix
Added `Log.e` calls (with the caught exception attached, so stack traces are preserved) at every
site that previously swallowed the error silently:
- `SafeFirestoreCall.kt`: both the `FirebaseFirestoreException` branch (now logs the mapped
  `e.code` alongside the exception) and the generic `catch (e: Exception)` branch.
- `FirestoreFlow.kt`: both `Query.snapshots()` and `DocumentReference.snapshots()` now log the
  listener's `error` before calling `close(error)`.

No behavior change — still returns the same `Result.Error`/closes the flow the same way. Purely
adding visibility.

## Build
`./gradlew assembleDebug lintDebug` both pass clean.
