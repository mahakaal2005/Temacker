# 2026-09-15 (session 5) — Fix audit inconsistency #5: Roster/ManageRoles mutations swallow errors

## Issue
`RosterViewModel`'s `removeMember`, `reassignMemberRole` (via `OnRoleSelected`), and `generateCode`
(`OnInviteClick`/`OnGenerateNewCodeClick`), and `ManageRolesViewModel`'s `createRole`, `updateRole`,
`deleteRole` were all bare `viewModelScope.launch { useCase(...) }` with no `.onFailure`/state
update — unlike `CreateProjectViewModel`/`LoginViewModel`, which correctly chain
`.onSuccess{}.onFailure{ error -> state.update }`. A Firestore permission error or network failure
on any of these six mutations silently no-opped with no feedback to the user.

## Fix
Matched the existing `error: UiText?` + `.onFailure { error -> _state.update { it.copy(error = ...) } }`
pattern already used by `CreateProjectViewModel`/`LoginViewModel`:
- `RosterState`/`ManageRolesState`: added `error: UiText? = null`.
- `RosterAction`/`ManageRolesAction`: added `OnErrorDismissed`.
- `RosterViewModel`: `removeMember`, `reassignMemberRole` (in `OnRoleSelected`), and
  `generateCode` now chain `.onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }`;
  `OnErrorDismissed` clears it.
- `ManageRolesViewModel`: `createRole`, `updateRole`, `deleteRole` do the same; `OnErrorDismissed`
  clears it.
- `RosterScreen`/`ManageRolesScreen`: added an inline error banner (`Text` + "Dismiss" `TextButton`)
  below the `TopAppBar`, matching `CreateProjectScreen`'s existing `error.asString()` pattern — no
  new snackbar/scaffold infrastructure introduced, since none exists elsewhere in the app.
- Added an error-state `@Preview` to both screens for the new banner.

## Build
`./gradlew assembleDebug lintDebug` both pass clean (two pre-existing, unrelated deprecation
warnings: `Icons.Filled.ArrowBack`, `LocalClipboardManager`).
