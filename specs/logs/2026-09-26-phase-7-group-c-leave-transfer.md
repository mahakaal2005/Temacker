# 2026-09-26 — Phase 7 Group C: leave, removal cleanup, leadership transfer

## Changed
- **Leave (#6):** `LeaveProjectUseCase`; `ProfileState.projectId/isLeader/isLeaveDialogVisible`, three `ProfileAction`s,
  `ProfileEvent.NavigateToProjectGate`, "Leave <project>" button + confirm dialog (Leader sees a hint instead), previews
  for the member and dialog states. `ProfileViewModel` suppresses the just-left project's transient permission errors
  (`isLeaving`, same idea as `isSigningOut`) until the selected project changes. `profileGraph`/`MainActivity` gained
  `onNavigateToProjectGate` (popUpTo the whole stack).
- **Transfer (#9):** rules `isLeaderPromotion`/`isLeaderDemotion` + `memberAfter` (`getAfter`), `MembershipRemoteDataSource`/
  `OfflineFirstMembershipRepository.transferLeadership` (reads roles, one batch, re-reads both members, upserts Room),
  `TransferLeadershipUseCase`, Roster "Make Leader" menu item + dialog + preview. "Can't be transferred" copy on the
  create-project screen updated. 9 new rules tests (105/105).
- **Cleanup (#7):** `functions/src/memberRemoval.ts` (pure planner, 4 tests) + `onMemberRemoved` trigger in `index.ts`;
  `notifications.ts` skips the decline push when `closedForUid == fromUid` (1 new test).

## Verified
Gradle build/lint/unit tests pass (72 cases, incl. 6 new use-case tests); `npm run test:rules` 105/105; functions
`npm test` 15/15 and `tsc --noEmit` clean.

## Not done yet
- **Deploy rules and functions** (`firebase deploy --only firestore:rules,functions`); transfer needs the rules, and
  cleanup needs the new function. Not deployed by me.
- On-device (two accounts): Leader transfers to the second account, old Leader can now leave, leaving the last project
  lands on No-project, removing a member returns their task to the Leader and closes their offers.
- Unverified assumption: the rules `getAfter` pairing passed the emulator, but batches of two members were only
  exercised there, not against production.
