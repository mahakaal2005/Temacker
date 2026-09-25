# Phase 7 — Multi-project hardening

Status: all four groups coded and tested 2026-09-26; rules/functions not yet deployed and nothing device-verified.

Phase 5b let one account belong to several projects, but only the switcher was built. Everything else still
assumes one project at a time. A code audit (2026-09-26) found 3 bugs and 9 missing features. The user approved
all four groups below. Build and verify one group at a time; log each in `specs/logs/`.

## Architecture changes (flagged per CLAUDE.md rule 2, approved with this spec)

1. ~~`PendingWriteEntity` gains a `userId` column~~ **Dropped (2026-09-26):** the sign-out wipe already closes the
   cross-account leak, and the column would need a Room migration that orphans writes queued at upgrade time.
2. New `DataError.Network.ALREADY_MEMBER` (mapped in `DataErrorToUiText`; produced from Firestore `ALREADY_EXISTS`).
3. New `LocalDataCleaner` interface in `core/domain` (impl `RoomLocalDataCleaner` in `core/data/database`), called on
   sign-out and account deletion; `SelectedProjectStore` gained `clear()`.
4. New Cloud Function `onMemberDeleted` in `functions/src/index.ts`.
5. Leadership transfer is now allowed. This reverses the "leadership can't be transferred" decision
   (`CreateProjectScreen.kt:99`), so that copy must change.
6. New Firestore rules: leadership swap, archived projects read-only for tasks/handoffs.

## Group A — bugs (#1, #2, #3, #10)

- Sign-out wipe: clear all Room tables and `SelectedProjectStore` on sign-out and account deletion.
- Duplicate join: `joinProject()` reads the member doc inside its transaction and fails with `ALREADY_MEMBER`;
  the Join screen shows "You're already in this project. Use the project switcher to open it." (no Switch button:
  the error can't carry the project id). Needs a rules change: `get` on your own member path is allowed even when
  the doc doesn't exist yet, otherwise the read errors and every first-time join breaks.
- Stale switcher: the collection-group membership snapshot now also upserts the user's memberships and, when the
  snapshot is from the server (not cache), deletes local memberships missing from it. Cache-served snapshots never
  delete, so a lagging snapshot can't undo a just-joined project (the Phase 5b race); the next snapshot heals it.

## Group B — Inbox and notifications (#4, #5)

- Badge counts waiting handoffs across all projects. The Inbox keeps its current-project sections and gains a
  "Waiting in <project> · N" section per other project (waiting-on-you only; "Earlier" stays current-project);
  tapping one selects that project first, then opens the offer. Uses the existing per-project queries, so no rules
  change. New `CurrentProjectProvider.observeUserProjectRefs()` + `ProjectRef` core model.
- Push payload carries `projectName`; the private notification shows it as the sub-text (the public lock-screen
  version doesn't).

## Group C — leaving, removal cleanup, transfer (#6, #7, #9)

- `LeaveProjectUseCase` (own uid through `removeMember`; the rules already allowed it) + a "Leave <project>" button and
  confirm dialog on the You tab. The Leader sees a hint to transfer leadership first instead of the button. Leaving the
  last project routes to the project gate (No-project screen).
- `onMemberRemoved` (Cloud Function, `onDocumentDeleted` on member docs, so it also covers Remove Member and account
  deletion): unfinished tasks held by the departed go to the Leader; every OFFERED handoff to or from them is closed as
  a DECLINED with reason "<name> left the project". Deviation: no new CANCELLED status (the app would have needed
  client changes) and no event written (Pulse only renders a fixed type allowlist). Auto-closed offers carry
  `closedForUid` so the departing member gets no push about their own offer.
- `TransferLeadershipUseCase` + "Make Leader" on the Roster menu (Leader-only, confirm dialog). One batch swaps the two
  memberships; the demoted doc carries `transferToUid` naming who the same batch promotes, and each write is checked
  against the other with `getAfter`, so a project can't end up with zero or two Leaders (9 rules tests).

## Group D — polish (#8, #11, #12)

- "Create a new project" on the Switch Project screen; `CreateProjectViewModel` selects the new project on success.
- Task and handoff create/update/delete denied by the rules when the project is archived (5 rules tests); reads stay open.
- Board shows "N queued changes in your other projects" (any queue row outside the selected project) that opens the
  switcher. Deviation: no separate all-projects queue screen; the existing per-project queue is reached by switching.

## Out of scope

All-projects export, enforcing the 200-task / 25-member caps, syncing name/photo changes into existing
memberships, a cap on projects per user.

## Testing

- `./gradlew assembleDebug lintDebug testDebugUnitTest` after every group.
- `npm run test:rules` for join, leave, transfer and archived-write rules; Jest for the functions.
- On device RZCWA28EAZF with two accounts, per group (see the plan in this phase's log).
- Previews for every new state.

## Completion

Tick each group in `specs/office/progress.md`, log the session in `specs/logs/`, mark this phase complete here.
