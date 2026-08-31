# 2026-08-27 — Real Firebase Auth + design system fix

## Context
Firebase project connected, real `google-services.json` in place. Two build errors were
fixed first (stray `c` typo in `app/build.gradle.kts`, and `firebase-auth` missing its
version because the BoM platform import had been stripped without removing the dependency).

## Done
- Swapped `InMemoryAuthRepository` for real Firebase Auth, per the TODO in `progress.md`:
  - `feature_auth/data/remote/AuthRemoteDataSource.kt` (interface) + `FirebaseAuthRemoteDataSource.kt`
    (impl): Credential Manager → Google ID token → `FirebaseAuth.signInWithCredential`.
  - `feature_auth/data/mapper/UserMapper.kt`: `FirebaseUser.toUser()`.
  - `feature_auth/data/repository/OfflineFirstAuthRepository.kt`: coordinates the remote
    source with `SessionManager` (DataStore), same contract as before.
  - `CoreModule.kt`: added `FirebaseAuth` and `CredentialManager` singletons.
  - `AuthModule.kt`: rebound to the real repository; `InMemoryAuthRepository` deleted.
- **Open blocker:** Google isn't enabled as a Sign-In provider in Firebase Console yet, so
  `google-services.json` has no Web client ID (`oauth_client` array is empty). Added
  `R.string.google_web_client_id` as a placeholder in `strings.xml` — sign-in will fail
  (`GetCredentialException`) until it's replaced with the real Web client ID from
  Firebase Console → Authentication → Sign-in method → Google → Web SDK configuration.
- Diagnosed the "UI is fucked up" report — two real causes, not a preview illusion:
  1. `res/values/themes.xml`'s XML theme parent was `android:Theme.Material.Light.NoActionBar`
     in an app that's supposed to be strictly dark — wrong window chrome/status bar contrast
     under Compose. Fixed to `Theme.Material.NoActionBar`.
  2. Every `@Preview` wrapped content in a bare `MaterialTheme { }`, which defaults to M3's
     light color scheme regardless of `UI_MODE_NIGHT_YES` — so the IDE preview never matched
     the real app. Built a real design system (`core/presentation/designsystem/`: `Color`,
     `Type`, `Shape`, `AppTheme.kt`) replacing the untouched Android Studio template
     (`ui/theme/`, deleted), and switched all previews to `TemackerTheme`.

## Next
- Enable Google as a Sign-In provider in Firebase Console, re-download `google-services.json`
  if needed, and paste the real Web client ID into `google_web_client_id`.
- Continue Phase 1: feature_project scaffold (Project, Role, Membership, InviteCode).
