# 2026-10-14 — Phase 9 committed, rules hardened before deploy

## Done
- Committed the uncommitted work as `2c60f04` (launch splash + launcher icon) and `26f14ee` (Phase 9).
- Checks before committing: `assembleDebug lintDebug testDebugUnitTest` pass (78 unit tests), functions Jest 17/17 and `tsc` clean. The first Gradle run failed on a corrupt local build-cache entry left by an interrupted build; `--no-build-cache` fixed it, no code change.
- A background security review of `26f14ee` flagged the rules. Fixed in `c7d7c83`, with 5 new emulator tests (133/133):
  - Critical: a joiner could write `extraPermissions` (even `deleteProject`) onto their own member doc. Joins now only accept the all-false map the app writes.
  - Role reassignment could change extras on the side; extras must now stay unchanged on that path.
  - Extras now accept only known permission keys.
  - A transfer request's `targetDisplayName` must match the target member's `displayName`, so an approver can't be shown the wrong name.

## Deployed
- The user ran `firebase deploy --only firestore:rules,firestore:indexes,functions --project temacker-a0252`. Rules and indexes released.
- All 3 functions were skipped as unchanged, so they had been deployed earlier even though progress.md said they hadn't. Flagged per rule 16; progress.md corrected.
- `.firebaserc` default is `demo-temacker` (the emulator project), so a bare `firebase deploy` fails. Always pass `--project temacker-a0252`.

## Decisions
- Reassignment stays unrestricted. Holders of `assignTasks` or `approveTaskRequests` can move any unfinished task, with no request needed and other fields editable.
- `core-splashscreen` dependency approved.

## Still open
- Not device-verified: Phase 7 groups A–D, Phase 9.
- Pre-existing: a `manageRoles` holder can edit a non-Leader role to include `deleteProject`.
- Play Store blockers: `com.example` applicationId, no release signing, release/Play SHA-1s in Firebase.

## Release setup (sideload via Innogeeks, not Play Store for now)
- applicationId changed to `com.temacker.app` (namespace stays `com.example.temacker`). Registered in Firebase via CLI (app `1:83858186212:android:e5973d93cfee79c23d31f1`) with debug and release SHA-1s; new `google-services.json` has Android OAuth clients for both.
- Release signing reads the gitignored `keystore.properties` (key at `~/keys/temacker-release.jks`, alias `temacker`, SHA-1 `72:F0:B0:C9:…:01:F9`). Without the file, release builds are unsigned.
- `assembleRelease` output verified with apksigner: signed by the release key, package `com.temacker.app`, versionCode 1.
