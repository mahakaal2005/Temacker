# 2026-09-26 — Phase 7 Group A: multi-project bugs

Spec: `specs/office/phase-7-multi-project.md`. Triggered by a multi-project gap audit the same day.

## Changed
- **#1 / #10 sign-out wipe:** new `LocalDataCleaner` (domain) + `RoomLocalDataCleaner` (`clearAllTables()` on IO, then
  `SelectedProjectStore.clear()`), bound in `CoreModule`, injected into `OfflineFirstAuthRepository`; runs after sign-out
  and after a successful account deletion. `SelectedProjectStore` gained `clear()` (DataStore impl + test fake).
- **#1 userId on pending writes: dropped** (see spec). The wipe covers the leak without a Room migration.
- **#2 duplicate join:** `joinProject()` reads the member doc in its transaction and throws `ALREADY_EXISTS`, mapped to
  new `DataError.Network.ALREADY_MEMBER` with its own message. `firestore.rules` members `get` split from `list` so a
  user can `get` their own not-yet-existing member path. 3 new rules tests (96/96 passing).
- **#3 stale switcher:** `ProjectRemoteDataSource.observeUserProjects` now returns `UserProjectsSnapshot` (projects,
  memberships, isAuthoritative). `OfflineFirstProjectRepository` upserts both and, only for server snapshots, deletes
  memberships missing from it via new `MembershipDao.deleteMissingForUser`, in one Room transaction.

## Verified
`./gradlew assembleDebug lintDebug testDebugUnitTest` pass; `npm run test:rules` 96/96.

## Not done yet
- **Deploy `firestore.rules` to `temacker-a0252` BEFORE installing this build.** Without the new `get` rule, the join
  transaction's member read is denied for non-members and every first-time join fails.
- On-device checks (RZCWA28EAZF, two accounts): sign out then sign in as the other account shows no cached data;
  rejoin with a second code shows the already-a-member message; removing a member drops the project from their switcher.
- No unit tests for the new repository logic (matches the deferred-tests precedent); flagged, not assumed.
