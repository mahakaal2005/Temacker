# 2026-09-17 (session 7) — Fix audit item #9 (2/4): RosterScreen missing preview states

## Context
Continuation of item #9. This sub-item: "`RosterScreen` covers only 1 of ~5 real UI states
(`isLoading`/`isInviteSheetVisible`/`reassignTargetUserId` dialog all un-previewed — note
`RosterScreen` already gained an error-state preview under item #5/#6, so just fill the remaining
gaps)."

## Fix
Added three `@Preview`s to `RosterScreen.kt`:
- `RosterScreenLoadingPreview` — `isLoading = true`.
- `RosterScreenInviteSheetPreview` — `isInviteSheetVisible = true` with a generated `inviteCode`,
  showing the `ModalBottomSheet`.
- `RosterScreenReassignDialogPreview` — `reassignTargetUserId` set on a non-Leader member, showing
  the `ReassignRoleDialog`.

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Per CLAUDE.md rule 14, no other item #9 screens were touched — ManageRolesScreen and HomeScreen land
as separate commits.
