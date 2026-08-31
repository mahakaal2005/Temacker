# 2026-08-31 — Firestore security rules, Google Sign-In finally fixed

## Context
Two things this session: (1) writing Phase 1's still-missing Firestore security rules, and
(2) debugging two bugs surfaced by manual testing — Google Sign-In still failing, and a new
`PERMISSION_DENIED` on create-project.

## Done
- Wrote `firestore.rules` (repo root) covering `projects`, `projects/{id}/roles`,
  `projects/{id}/members`, `inviteCodes` — matched exactly against the field shapes in
  `feature_project/data/mapper/*Mapper.kt` and the reads/writes in
  `feature_project/data/remote/*RemoteDataSource.kt`. Enforces membership-gated reads, role/perm
  shape validation on create (can't self-elevate permissions), and Leader immutability. Deployed
  by the user via Firebase Console; not yet tested against the emulator.
- Debugged Google Sign-In (`systematic-debugging` skill) through several rounds of evidence:
  - `NoCredentialException` on both the silent and full-picker attempts, even with a Google
    account present on-device → fixed by switching the no-authorized-account fallback from
    `GetGoogleIdOption(filterByAuthorizedAccounts=false)` to `GetSignInWithGoogleOption`
    (button-flow-specific, handles the full picker more reliably).
  - That surfaced a second, misleading failure: `GetCredentialCancellationException: [16]
    Account reauth failed` after selecting an account — traced to the fact that this catch
    block was the *only* one not logging, which is why earlier logcat filters came back silent.
    Added logging there.
  - Actual root cause (confirmed by user): **wrong SHA-1 fingerprint registered in Firebase.**
    Not a code bug at all — the prior session's progress.md note claiming SHA-1 was "independently
    verified correct" was wrong. Fixed by the user directly in the Firebase Console.
- Hardened the sign-in flow while in there: nonce is now SHA-256-hashed before `setNonce()`
  (was being passed raw, which did nothing — Google's guidance is to hash it so the ID token's
  `nonce` claim can't be replayed).
- Removed `MainActivity.kt`'s `printSha1()` debug scaffolding (logged the app's SHA-1 to logcat
  on every launch; added mid-session to diagnose the above, no longer needed).
- Reviewed Firebase-adjacent hygiene: `google-services.json` correctly gitignored, Firebase BoM
  already in use. Flagged but did not touch (out of current phase scope): `backup_rules.xml` /
  `data_extraction_rules.xml` are still the stock empty template, so Android 12+ full-data
  backup includes the DataStore-persisted `session_uid`; release build type has
  `optimization { enable = false }` (R8 off).

## Still open
- The create-project `PERMISSION_DENIED` reported at the start of this session was never
  root-caused — debugging got diverted into Google Sign-In. Need fresh logcat
  (`package:mine tag~:Firestore|PERMISSION_DENIED|temacker`) reproduced against the now-deployed
  `firestore.rules`, ideally with email/password auth to keep it isolated from Google Sign-In.
- Firestore rules still untested against the emulator.
- On-device golden-path click-through (user doing this manually) and ViewModel unit tests both
  still outstanding per `progress.md`.

## Next
- Re-chase the create-project permission error with logcat.
- Once auth + create-project are both confirmed working, do the full golden-path walkthrough.
