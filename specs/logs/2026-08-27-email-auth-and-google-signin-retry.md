# 2026-08-27 (cont'd) — Email/password auth, reverted to Credential Manager

## Context
Google Sign-In kept failing through two different implementations (Credential Manager's
`NoCredentialException`, then legacy `GoogleSignInClient`'s `ApiException` statusCode=10 /
DEVELOPER_ERROR) despite every verifiable config value (SHA-1, OAuth client, Web client ID,
consent screen publishing status) checking out correct. Used Context7 to pull current official
Firebase docs rather than go from memory, which confirmed Credential Manager (not the legacy
client) is still the actual current-recommended approach — so reverted to it, kept the real
bug fix from the detour (Activity-context via `CurrentActivityHolder`), and added email/password
auth as a working alternative per request.

## Done
- Reverted `FirebaseAuthRemoteDataSource` from legacy `GoogleSignInClient` back to Credential
  Manager, keeping `CurrentActivityHolder` (feeds the foreground Activity instead of the
  Application context Koin injects elsewhere — genuine bug, independent of which Google
  Sign-In library is used).
- Added the official two-step pattern: try `filterByAuthorizedAccounts(true)` first (silent
  sign-in for returning users), fall back to `false` (full account picker) on
  `NoCredentialException` — better first-time/sign-up support than the single-shot `false` we
  had before.
- Added email/password auth: `AuthRepository`/`AuthRemoteDataSource` gained
  `signInWithEmail`/`registerWithEmail`; new `SignInWithEmailUseCase`/`RegisterWithEmailUseCase`
  (replacing the old single `LoginUseCase`, split same as `SignInWithGoogleUseCase`).
  Registration calls `sendEmailVerification()` fire-and-forget (free, no paid tier) — failure
  to send doesn't block account creation.
- Login screen: email/password fields, sign-in/register mode toggle, Google button below an
  "OR" divider. New preview for register mode.
- Gradle: reverted `play-services-auth` back to `androidx.credentials` +
  `credentials-play-services-auth` + `googleid`.

## Open issue — Google Sign-In still unverified as working
Last real test (before this revert) failed at the Firebase/Google backend level, not in app
code — both a Credential Manager implementation and a structurally different legacy-API
implementation failed with "not authorized" style errors against independently-verified-correct
config. Leading theory: propagation delay on a same-day-created OAuth client. This revert
didn't change any config, only which client library issues the request, so **Google Sign-In
needs to be retested** — it may now work purely because enough time has passed, or may still
be blocked. Email/password is a working, tested-independent fallback either way.

## Next
- Retest Google Sign-In; if still failing, get a fresh Logcat under tag `FirebaseAuthRemote`.
- Continue Phase 1: feature_project scaffold (Project, Role, Membership, InviteCode).
