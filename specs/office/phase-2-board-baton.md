# Phase 2 — Board and the Baton

Status: implemented, pending on-device verification and Firestore rules deploy (see
`specs/office/progress.md`).

Task board with a holder + handoff model: one person holds a task at a time and explicitly hands it
to someone else, who accepts or declines. Not a status-column kanban.

**Prerequisite:** `specs/ultimate_android_architecture.md` Data Model + Phase Roadmap must be
rewritten to the holder/handoff model before writing any `feature_tasks` code (see
`specs/office/implementation-plan.md`).

## Screens / Flows

- **Board** — tabs: To do / Doing / Done. Cards are holder-led; amber ring = handoff in flight, softer ring = queued offline.
- **Empty board** — receiving-arc drawn dashed: "nothing to hand over yet."
- **New task** — title/notes/due date; creator becomes the initial holder.
- **Task detail + baton trail** — append-only trail: amber node = offered, teal = accepted, hollow ring = still open.
- **Hand-off sheet** — member picker + note, offers the task to another member.
- **Handoff to you** — incoming offer, accept/decline.
- **Decline** — requires a reason.
- **Read-only board** — default role (no `assignTasks`/`editAnyTask`): no FAB, flatter cards, names who can fix it.
- **Delete confirm** — permanent; the project keeps an attributable event record.

## Data Model

`tasks/{id}`, `handoffs` subcollection, `events` collection — see architecture doc Data Model section
(once rewritten per the prerequisite above).

## Files (representative)

- `feature_tasks/domain/model/{Task,Handoff}.kt`, `repository/TaskRepository.kt`, `use_case/{CreateTask,OfferHandoff,AcceptHandoff,DeclineHandoff,DeleteTask,ObserveBoard}UseCase.kt`
- `feature_tasks/data/remote/FirestoreTaskRemoteDataSource.kt`, `data/local/{TaskDao,TaskEntity}.kt`, `data/repository/OfflineFirstTaskRepository.kt`
- `feature_tasks/presentation/{board,task_detail,handoff,new_task}/*`
- `feature_tasks/di/TasksModule.kt`
- `firestore.rules` — only current holder offers a handoff; only offered `toUid` accepts/declines; `handoffs`/`events` create-only.

## Testing

- Offer/accept/decline transitions update `holderUid` and append to the trail correctly.
- Read-only board hides actions when the member's permission snapshot lacks `assignTasks`/`editAnyTask`.
- Offline: task/handoff writes queue and later sync; UI reflects Room immediately.

## Out of scope for Phase 2

Notifications/push, inbox badge, offline-queue visibility screen (Phase 3), team analytics (Phase 4).

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).

**Implemented 2026-09-17** — all screens, data/domain layers, DI, nav, and Firestore rules built and
compiling (`./gradlew clean assembleDebug lintDebug` green; 62/62 Firestore rules tests passing).
Firestore rules deployed to production (2026-09-17, `temacker-a0252`). On-device golden path partially
verified 2026-09-18 (create/mark-done/delete confirmed working; hand-off/accept/decline and the
Default-role read-only board need a second project member, not yet available). Two things remain
before this phase can be marked fully complete: unit tests for handoff transitions (deferred, matching
Phase 1's precedent), and the second-member-dependent verification above. See
`specs/logs/2026-09-17-phase-2-implementation.md` and `specs/logs/2026-09-18-phase-2-device-verification.md`
for the full session logs.
