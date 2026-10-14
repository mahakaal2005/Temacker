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

## Crashlytics + Analytics
- Added `com.google.firebase.crashlytics` plugin 3.0.8 plus `firebase-crashlytics` and `firebase-analytics` (BoM 34.5.0), approved by the user.
- `App.kt` turns collection off in debuggable builds and sets the Firebase uid as the user ID (no name or email).
- `core/data/firebase/CrashReporting.kt`: `reportIfUnexpected()` records non-fatals from `safeFirestoreCall` and both snapshot listeners, skipping UNAVAILABLE/DEADLINE_EXCEEDED.
- Verified: debug + release builds, lint, 78 unit tests pass; release on RZCWA28EAZF logs "Initializing Firebase Crashlytics 20.0.3 for com.temacker.app" and settings status "activated".
- Screen views: `MainActivity` logs `screen_view` on each destination change with the short route name (e.g. `Board`, `TaskDetail`), never route args. Verified on device with Analytics debug mode (Splash → Login events seen).
- Verified by the user: Google sign-in works in the signed release build of `com.temacker.app`, and Crashlytics shows "App detected" for it.

## Fix: fresh sign-in landed on No-project despite having projects
- Root cause: `ProjectGateViewModel` decided on the first Success of `observeUserProjects()`, which is Room's emission. On a fresh install Room is empty, and that emission races ahead of the Firestore listener's write, so the gate saw `[]`. Earlier fixes only covered phones that already had a cache.
- Fix: `ProjectRepository.syncUserProjects()` waits (10 s timeout) for the first authoritative (server, not cache) snapshot and writes it to Room. The gate goes straight to Home on a non-empty cache; with an empty cache it syncs first, then decides. Offline with an empty cache still falls back to No-project.
- `ProjectGateViewModelTest`: 4 cases; the fresh-install case failed before the fix. 82/82 unit tests, debug + release builds and lint pass.

## Fresh sign-in gate: server read instead of listener metadata
- The first gate fix still took 10.2 s on device, exactly the timeout. The listener's switch from cache to server is a metadata-only change, which is never delivered without `MetadataChanges.INCLUDE`. `syncUserProjects()` now uses `ProjectRemoteDataSource.fetchUserProjects()` (`get(Source.SERVER)`). Measured on device after the change: 3.7 s from gate to Board on a fresh install.
- R8 is on for release (mapping in the bundle, `proguard-rules.pro` keeps line numbers), with `ndk.debugSymbolLevel = SYMBOL_TABLE` and versionCode 2. APK 19.7 MB → 4.4 MB. Play's native-symbols warning may persist: the only `.so` files are AndroidX's prebuilt, stripped `graphics.path` and `datastore_shared_counter`.

## Loading states
- User asked for loading screens that say what is happening. `LoadingState(icon, title, body)` and `SkeletonList(caption)` are in `core/presentation/components/LoadingState.kt`, with a 400 ms text delay, a 6 s slow-connection hint, a polite live region, and reduced-motion support. Rolled out to the gate, 9 detail screens and 8 list screens. InvitedFirstRun's loading moved outside its scroll so it centres.
- Noticed, not fixed (rule 14): deprecated `Icons.Rounded.Send` (PulseScreen) and `Icons.Rounded.Assignment` (BoardScreen) warnings, both pre-existing.

## Swipe the pill to switch projects
- Spec: follow-up section in `phase-5b-project-switcher.md`. The user chose a bounce at the ends (no wrap-around) and a peek of the neighbour while dragging. They approved the first ViewModel in core (`core/presentation/switcher/ProjectSwitcherViewModel`, which uses only `CurrentProjectProvider` and `SelectedProjectStore`), and the architecture spec §4 now notes the allowance.
- `SwitcherPill` gained `previousProjectName`/`nextProjectName` and the swipe callbacks. Faces slide via `graphicsLayer`, a custom `Layout` sizes the slot to the current face only, a release past 40% or a fling commits, there's a tick haptic, and TalkBack gets custom actions. If the new name hasn't arrived after 4 s, it slides back. Wired into Board, Inbox, Team and You.
- Bug found on device: the peek promised one project and the switch landed on another. `ProjectDao.observeActiveByIds` had no ORDER BY, and Room's REPLACE upserts reshuffle row order, so the project order drifted between emissions (the Switch project list too). Fixed with `ORDER BY createdAt, id` (query only, no migration).
- Verified on RZCWA28EAZF: up/down switches in under 0.5 s with the pill and board agreeing, swiping up on the last project bounces and stays, and Team switches too. 6 new ViewModel tests, 88/88 overall.

## Privacy policy and account-deletion pages (Play requirement)
- Free Firebase Hosting on `temacker-a0252`: `hosting/public/` (index, privacy, delete-account, style.css), with `firebase.json` hosting config and `cleanUrls`. Deployed by the user's go-ahead with `firebase deploy --only hosting --project temacker-a0252`.
- Live: https://temacker-a0252.web.app/privacy and https://temacker-a0252.web.app/delete-account (both return 200).
- Content was checked against the code: account deletion removes the Auth user, memberships and this device's FCM token, and wipes local data; it is blocked for Leaders. Contact is the user's personal Gmail for now; the user plans to change it later.

## Play Console setup
- App content is all done (Play shows "You're all caught up"). Data safety's per-type answers had not been saved before, so all 9 were filled again and submitted:
  - Name, email and other info (the profile photo link): app functionality and account management.
  - User IDs: the same plus analytics, because the uid is set as the Crashlytics/Analytics user ID.
  - Other user-generated content: app functionality.
  - App interactions, crash logs and diagnostics: analytics.
  - Device IDs (FCM token and app instance ID): app functionality and analytics.
  - Every type is collected, not shared, not ephemeral, and required.
- Open testing is locked until production access. New personal accounts need a closed test with at least 12 testers for 14 days first. The user chose a closed test with a Google Group, so testers join without handing over their emails. The group is `temacker-testers@googlegroups.com`: anyone can join, only the owner can post, and only the owner sees the member list. It is waiting on a CAPTCHA the user has to solve.
- Closed testing (Alpha):
  - India is targeted.
  - The release has versionCode 2 (uploaded from this machine) and release notes.
  - Saved as a draft. It can't be sent until the testers and the store listing screenshots are in.
- Store settings: category is Productivity; contact is the user's Gmail and https://temacker-a0252.web.app.
- Default store listing (draft):
  - name, short and full description
  - a 512 icon rendered from the adaptive-icon vectors
  - a 1024×500 feature graphic built from brand tokens
  - Still needed: two or more phone screenshots, which need the phone on adb.
- In-app "Privacy policy" row on the You tab opens the hosted page (`36e18c6`).
- The user solved the CAPTCHA, so the group exists. Closed testing testers are set to the group, with feedback going to the user's Gmail.
- Store listing: 4 phone screenshots (Board, Task, Team and You, cropped to 1080×2160 for Play's 2:1 limit), taken with adb on RZCWA28EAZF from the reviewer account. Only the feature graphic is labelled as made with AI, because Claude wrote the code that drew it.
- The category had not saved the first time because the dialog's Save was never clicked. It is now saved as Productivity.
- The release preview has no errors, only the native-symbols warning. The release is saved, and Publishing overview has 14 changes ready to submit, waiting on the user's OK.
- Play's app-signing SHA-1 is now registered in Firebase (3 SHA-1s: debug, upload and Play signing).
