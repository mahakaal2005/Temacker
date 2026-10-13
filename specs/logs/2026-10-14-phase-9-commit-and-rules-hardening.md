# 2026-10-14 — Phase 9 committed, rules hardened before deploy

## Done
- Committed the uncommitted work as `2c60f04` (launch splash + launcher icon) and `26f14ee` (Phase 9).
- Checks before committing: `assembleDebug lintDebug testDebugUnitTest` pass (78 unit tests), functions Jest 17/17 and `tsc` clean. The first Gradle run failed on a corrupt local build-cache entry left by an interrupted build; `--no-build-cache` fixed it, no code change.
- A background security review of `26f14ee` flagged the rules. Fixed in `c7d7c83`, with 5 new emulator tests (133/133):
  - Critical: a joiner could write `extraPermissions` (even `deleteProject`) onto their own member doc. Joins now only accept the all-false map the app writes.
  - Role reassignment could change extras on the side; extras must now stay unchanged on that path.
  - Extras now accept only known permission keys.
  - A transfer request's `targetDisplayName` must match the target member's `displayName`, so an approver can't be shown the wrong name.

## Still open
- Not deployed: Phase 7 + 9 rules, indexes and functions to `temacker-a0252` (waiting on user approval).
- Not device-verified: Phase 7 groups A–D, Phase 9.
- Open design question: holders of `assignTasks` or `approveTaskRequests` can reassign any unfinished task, and that update branch doesn't freeze status/description/due date.
- Pre-existing: a `manageRoles` holder can edit a non-Leader role to include `deleteProject`.
- `core-splashscreen` dependency was added in an earlier session with no recorded approval (rule 10).
- Play Store blockers: `com.example` applicationId, no release signing, release/Play SHA-1s in Firebase.
