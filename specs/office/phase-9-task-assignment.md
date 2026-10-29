# Phase 9 — Task assignment, JSON import, drop/transfer requests, grantable permissions

Status: approved 2026-09-26 by the user (plan mode). Merges `phase-9-extra-permissions.md`.

Today a task can only be created for yourself (`NewTaskViewModel` sets holder = current member; rules require `holderUid == auth.uid`). The Leader can't assign, import or reassign tasks, and members can't ask to drop or transfer one. Permissions are role-only, so they can't be delegated per person. This phase fixes those. It also folds in the deferred per-member extra permissions (`specs/office/phase-9-extra-permissions.md`).

Decisions from the user: approval is needed only for drop and transfer requests; JSON import matches assignees by email and leaves unmatched ones unassigned; extra permissions are folded in.

Paths below are relative to `app/src/main/java/com/example/temacker/` unless noted. CLAUDE.md rules 2 and 3 apply: the spec must be written to `specs/office/phase-9-task-assignment.md` and approved before code, and the Phase 9 extras doc is merged into it.

## Step 0: spec and architecture changes (need approval)
- New spec file; update `specs/office/progress.md`; mark the old `phase-9-extra-permissions.md` as merged.
- Architecture changes to flag:
  - New permission flags on `RolePermissions`: `importTasks`, `approveTaskRequests`. `assignTasks` (already exists) covers assigning and reassigning tasks.
  - New `extraPermissions` field on membership (Room `@AutoMigration`, mappers, remote data source).
  - New `Handoff.kind`: OFFER (existing), DROP_REQUEST, TRANSFER_REQUEST, plus a `PENDING_APPROVAL` state.
  - Unassigned tasks (`holderUid` null) and `createdByUid` split from `holderUid`.
  - Firestore rule changes and a Cloud Function trigger update.

## Group A: permissions plumbing
- Add the two flags to every mirror: `RolePermissions.kt`, `RoleEntity`/`MembershipEntity`, `RoomMapper.kt` (positional, lines ~20-34), `RolePermissionsMapper`, `RoleCapabilities.kt` (rebuild the stale explainer), `ManageRolesScreen`, `firestore.rules` (`fullPermissions`/`noPermissions`, `hasPermission()` must read role OR extras), `firestore-tests/rules.test.js`.
- Extra permissions: add-only, effective = role ∪ extras. `deleteProject` is not grantable. Only holders of `manageRoles` can grant. UI: "Extra permissions" sheet from the Roster member menu, following the Phase 9 doc.

## Group B: Leader assigns a task to a person
- `NewTaskScreen`/`NewTaskViewModel`: assignee picker (members list), shown only with `assignTasks`; default is yourself, with an "Unassigned" option.
- `FirestoreTaskRemoteDataSource.createTask`: stop setting `createdByUid = holderUid`.
- Rules: task create allows `holderUid` to be any member when the caller has `assignTasks`; `createdByUid == auth.uid` stays.
- Reassign: a "Reassign" action on a task for `assignTasks` holders. New rule branch on task update: holder change by `assignTasks`, with the `timesHandedOver` bump.
- Push for "assigned to you" via `onTaskWritten` in `functions/src/index.ts`.

## Group C: JSON import
- Reuse the Phase 6 export schema shape (`core/domain/model/ProjectExportData.kt`, `FileExportWriter.kt`) as the reference. New reader: `ImportTasksUseCase` and a parser. Needs `ActivityResultContracts.OpenDocument` (there is no picker code today).
- Format: `{"tasks":[{"title","description","dueDate","assigneeEmail"}]}`.
- Preview screen listing tasks and matched assignees; unmatched emails become unassigned and are flagged; confirm creates them in one batch. Gated by `importTasks`.

## Group D: drop and transfer requests
- Member requests DROP (task returns to unassigned or the Leader) or TRANSFER (to a chosen member). Both go to approvers (`approveTaskRequests`, the Leader by default). The Leader's own drop or transfer needs no approval.
- Reuse the `Handoff` model and Inbox: add `kind`; approvers see requests in `Inbox.from` under "Waiting on you" (new `InboxRowKind`s); approve applies the change in a transaction, decline stores a reason. Reuses `AcceptHandoffUseCase`/`DeclineHandoffUseCase` patterns in `FirestoreTaskRemoteDataSource.kt`.
- Rules: handoff create allowed for the current holder with the new kinds; update allowed for approvers; task holder change only through an approved request or `assignTasks`.
- Push: extend `planForHandoffChange` in `functions/src/notifications.ts` for the new kinds; extend `memberRemoval.ts` to cancel pending requests.

## Verification
- `./gradlew assembleDebug lintDebug testDebugUnitTest` after each group; add unit tests for the import parser, effective-permission logic and Inbox mapping.
- `npm run test:rules` with new emulator cases for each rule change; Jest tests for the functions; redeploy rules and functions to `temacker-a0252`.
- On device RZCWA28EAZF (two accounts): Leader assigns a task and reassigns it; import a sample JSON with matched and unmatched emails; member requests a drop and a transfer, the Leader approves and declines; a member granted `assignTasks` as an extra can assign.
- Log the session in `specs/logs/` and tick `progress.md`.
