# 2026-09-15 (session 4) — Fix `updateRole()` isLeader corruption bug

## Context
Bug #1 from the prior session's 4-module code audit
(`specs/logs/2026-09-15-manage-roles-reassign-fix-and-full-audit.md`), fixed in isolation per user
request — one item at a time, review before moving to the next.

## Bug
`OfflineFirstRoleRepository.updateRole()` hardcoded `isLeader = false` when writing the post-update
role into Room instead of preserving the role's real `isLeader` value. Root cause:
`RoleRemoteDataSource.updateRole()` returned `EmptyResult<DataError>` (no data back), so the
repository fabricated a local `Role` instead of using real data.

While fixing this, found the bug was actually worse than described: `FirestoreRoleRemoteDataSource
.updateRole()` used `.set()` with a full field map that also hardcoded `isLeader = false`, so a call
against the Leader role would have corrupted `isLeader` in Firestore itself, not just Room.

## Fix
Same pattern as the `reassignRole` fix from the prior session:
- `RoleRemoteDataSource.updateRole()` / `FirestoreRoleRemoteDataSource.updateRole()`: return type
  changed to `Result<Role, DataError>` (mirrors `createRole`). Firestore write switched from a full
  `.set()` overwrite to a partial `.update()` of only `name`/`permissions`, then re-reads the doc and
  maps it back via `toRole()` — same shape as `reassignRole`'s member re-read.
- `OfflineFirstRoleRepository.updateRole()`: now does
  `remote.updateRole(...).onSuccess { roleDao.upsertAll(listOf(it.toEntity())) }.asEmptyResult()`
  instead of building a fake `Role(..., isLeader = false)`. `RoleRepository`'s domain interface
  still returns `EmptyResult<DataError>`, so `.asEmptyResult()` converts at the repository boundary.

## Verification
`./gradlew assembleDebug lintDebug` — both pass clean. Not re-tested on-device this session (no
code path change visible to the UI beyond correctness of the underlying data — Leader role updates
are already blocked in the UI, so this fix mainly hardens against future logic bugs).

## Not touched this pass (per explicit scope)
Every other item from the audit list — sign-in-cancel false error, collapsed auth errors, silent
Firestore exception swallowing, Roster/ManageRoles/Profile error-surfacing gaps, Room indices/cascade
delete, `DataError` alignment, missing previews, style items — untouched, to be picked up one at a
time in future sessions per the audit log's "Next" list.
