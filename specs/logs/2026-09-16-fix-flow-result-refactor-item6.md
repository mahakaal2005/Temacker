# 2026-09-16 (session 6) — Fix audit inconsistency #6: Flow<Result<T, DataError>> refactor

## Context
Continuation of the one-by-one audit cleanup from `specs/logs/2026-09-15-manage-roles-reassign-fix-and-full-audit.md`.
Items #1–#5 were fixed in earlier sessions. This session tackled item #6, originally stated as:
"Profile screen has no error-surfacing path at all — `ProfileState` has no `error: UiText?` field
and `ProfileEvent` has no error/snackbar case, so failures from `observeUserProjects`/
`observeCurrentMembership` have no way to reach the UI."

## Investigation
Before touching code, dispatched two research passes (read-only) to check the premise:
- `observe*` methods return plain `Flow<T>` today, not `Result`-wrapped.
- Every offline-first repository (`OfflineFirstProjectRepository`, `OfflineFirstMembershipRepository`,
  `OfflineFirstRoleRepository`, `OfflineFirstInviteCodeRepository`) already isolates Firestore
  listener errors with an **empty** `.catch { }` on the remote-sync branch inside its `channelFlow`.
  The Room-backed branch (what the caller actually receives) is unaffected. This is correct
  offline-first behavior per the `android-data-layer` skill ("Room as single source of truth... the
  ViewModel never observes network responses directly") — **no crash risk exists today**, and no
  failure was ever reaching Profile (or any other screen) in the first place. The real, narrower gap
  was that those six `.catch { }` blocks logged nothing at all.

Presented with a choice — (a) just log the six swallowed errors (minimal), or (b) do the full
`Flow<Result<T, DataError>>` refactor so sync failures actually reach every screen's state, matching
the `android-error-handling` skill's `Result`/`onSuccess`/`onFailure` conventions used everywhere
else — **the user explicitly chose (b)**.

## Fix
- `core/data/firebase/SafeFirestoreCall.kt`: extracted the `FirebaseFirestoreException.Code →
  DataError.Network` mapping into a reusable `Throwable.toFirestoreDataError()`, used both by
  `safeFirestoreCall`'s existing catch block (no behavior change there) and the new Flow `.catch{}`
  blocks below.
- Domain repository interfaces (`ProjectRepository`, `MembershipRepository`, `RoleRepository`,
  `InviteCodeRepository`): all six `observe*` methods now return `Flow<Result<T, DataError>>`
  instead of `Flow<T>`.
- The four offline-first repositories: each `.catch { }` (previously empty) now
  `send(Result.Error(e.toFirestoreDataError()))`; each Room-backed emission is now wrapped
  `Result.Success(...)` before `send()`.
- Every ViewModel collecting one of these flows (`HomeViewModel`, `RosterViewModel`,
  `ManageRolesViewModel`, `ProfileViewModel`) now unwraps with
  `.onSuccess { ... same body as before ... }.onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }`
  — `.onFailure` only ever sets `error`, never clears already-loaded data, so a transient sync
  hiccup shows a dismissible banner without blanking the screen (offline-first guarantee preserved).
- `HomeState`/`ProfileState` gained `error: UiText? = null`; `HomeAction`/`ProfileAction` gained
  `OnErrorDismissed`; `HomeScreen`/`ProfileScreen` gained a dismissible error banner (same pattern as
  item #5's Roster/ManageRoles banners) plus a new error-state `@Preview`. `RosterState`/
  `ManageRolesState`/`RosterAction`/`ManageRolesAction` already had `error`/`OnErrorDismissed` from
  item #5 — reused as-is.
- `ProjectGateViewModel`'s one-shot `observeUserProjects().first()` was changed to
  `.filterIsInstance<Result.Success<List<Project>>>().map { it.data }.first()` — since the
  `channelFlow`'s two branches (Firestore-sync-error vs Room-data) now race, a plain `.first()`
  could nondeterministically pick up a transient `Result.Error` blip instead of waiting for real
  Room data (previously `.first()` only ever saw Room-branch emissions since the sync branch never
  called `send()`). No state/error UI was added here — it's a one-shot routing decision, not a
  screen.

## Build
`./gradlew assembleDebug lintDebug` both pass clean after a `./gradlew clean` (an initial build hit
a false-positive cascade of "cannot infer type"/"unresolved reference" errors across every touched
ViewModel — turned out to be a stale incremental-compilation cache, not a real code issue; a clean
build compiled correctly on the first try).

## Scope
Per CLAUDE.md rule 14, no other audit items were touched. This was a genuinely large, atomic
interface change (~17 files) since `observe*`'s return-type change requires every call site updated
in the same commit to compile — could not be split into multiple independently-compiling commits.
