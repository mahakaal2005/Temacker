# 2026-09-17 (session 8) — Fix stale-UI-after-mutation, part 2: listener churn

## Context
Continuation of the stale-UI-after-mutation fix (see
`specs/logs/2026-09-17-fix-stale-ui-room-reconciliation.md` for bug A / background). This commit
fixes bug B.

## Fix
**Bug B — nested per-project listeners restarted on every unrelated re-emission.**
`RosterViewModel`, `ManageRolesViewModel`, `HomeViewModel`, `ProfileViewModel` all wrapped their
nested `observeMembers`/`observeRoles`/`observeCurrentMembership`/`observeActiveInviteCode`
collectors inside `observeUserProjects().collectLatest { ... }`. `collectLatest` cancels and
restarts everything inside it on *every* new emission — including emissions carrying the exact same
project list, which happen often since `observeUserProjects()`'s own remote-sync branch continuously
re-upserts on any Firestore echo. Every restart re-registered a brand-new Firestore listener (a
fresh query, not incremental), widening the window in which a not-yet-fully-consistent snapshot
could arrive and trigger bug A's stale-data symptom.

Restructured all four ViewModels' `init` blocks: the outer `observeUserProjects()` collection now
only updates `projectId`/`projectName`/top-level `error` state (plain `.collect`, nothing nested to
cancel). A separate `projectId` `Flow` (`mapNotNull { (it as? Result.Success)?.data?.firstOrNull()?.id }.distinctUntilChanged()`)
drives each nested observer independently via `.flatMapLatest { observeX(it) }` — so a nested
listener only restarts when the project id actually changes, not on every re-emission of the same
project. Applied consistently to all four ViewModels (not just Roster/ManageRoles, where the
symptom was originally observed) since leaving the same known-bad pattern in half of them would
leave the bug half-fixed. Requires `@OptIn(ExperimentalCoroutinesApi::class)` on each class, matching
existing precedent (`OfflineFirstProjectRepository` already uses `flatMapLatest` this way).

`ProfileViewModel`'s separate `authRepository.observeUser().collectLatest { ... }` collector is
unrelated (no nested listeners) and untouched.

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Together with the previous commit's Room reconciliation fix, this closes the root cause of the
stale-UI-after-mutation symptom. On-device verification (confirming the original repro no longer
reproduces) happens later in this session's verification phase, after the Firestore rules test
suite is also built, per the plan's coding-first ordering.
