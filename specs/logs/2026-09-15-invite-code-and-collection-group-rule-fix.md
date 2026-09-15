# 2026-09-15 (session 2) — collection-group rule bug persisted, invite code generation broken

## Context
User reported still being unable to create projects and asked to test the invite-code flow. Found
the installed APK predated the previous session's four-bug fix commit (`c5a2990`) — rebuilt and
reinstalled first. Project creation itself then worked fine, but logcat showed the exact
`collectionGroup("members")` `PERMISSION_DENIED` that the previous session's log claimed was fixed.

## Done
- Rebuilt + reinstalled the app (device had a stale pre-fix build); confirmed project creation
  works.
- Found the previous session's bug-#2 "fix" (`allow read: if isMember(projectId) ||
  resource.data.userId == request.auth.uid`) never actually worked — it was masked by the same
  session's bug-#4 fix (`createProject()` seeding `membershipDao` directly), so the golden-path
  test never actually exercised the collection-group listener succeeding.
- Root cause: Firestore's list/collectionGroup query validator rejects a rule outright if it
  contains any `exists()`/`get()` call it can't resolve for that query shape — even one guarded by
  an OR with an otherwise-provable branch. `isMember(projectId)` is resolvable for roster's direct
  `projects/{projectId}/members` query (projectId is a concrete path segment) but not for
  `observeUserProjects()`'s collectionGroup query (projectId varies per candidate document).
- Fixed properly: added a dedicated top-level `match /{path=**}/members/{userId} { allow list: if
  resource.data.userId == request.auth.uid; }` block. Cloud Firestore ORs together all matching
  rule blocks for a path, so this authorizes the collectionGroup query on its own while the nested
  block (unchanged, still `isMember(projectId) || resource.data.userId == request.auth.uid`)
  continues to govern direct project-scoped access.
- Verified via fresh app relaunch: no `PERMISSION_DENIED` in logcat, app routes straight to Home for
  an existing member — this also verifies sign-out/re-entry routing, previously unexercised.
- Tested invite-code generation (Roster → invite icon): stuck indefinitely on "Generating...".
  Traced to `generateInviteCode()`'s first step, a `list` query (`.get()` with
  `whereEqualTo(projectId)` + `whereEqualTo(isActive, true)`) on the root `inviteCodes` collection,
  governed by `allow list: if isMember(resource.data.projectId)` — same class of bug as above, but
  `inviteCodes` isn't nested under `/projects/{projectId}`, so there's no path-segment projectId to
  resolve `isMember()` against, and no resource.data-only substitute exists either (unlike the
  members case, there's no field we can compare directly to `request.auth.uid`).
  `RosterViewModel.generateCode()` calls the use case fire-and-forget and doesn't surface the
  resulting `Result.Error`, so the failure was silent — no error UI, no log line.
- Fixed by loosening `inviteCodes`' `allow list` to `isSignedIn()` (same trust boundary as the
  existing `allow get: if isSignedIn()`) — accepted as a real, flagged security loosening rather
  than a data-model change, given Phase 1 scope. Verified: invite code `TMK-TB39-5H` generated
  successfully on-device after redeploying.
- Both rules changes deployed via `firebase deploy --only firestore:rules`, zero warnings.

## Still open
- Invite-code *join* flow (the joiner's side) not tested — needs a second Google account/device.
- Manage roles, remove member, reassign role still not exercised.
- ViewModel unit tests not started.
- Firestore rules not tested against the emulator — this session's bugs (both instances of "list
  query validator rejects rules with unresolvable get()/exists()") would have been caught
  immediately by emulator-based rule unit tests instead of manual on-device discovery. Worth
  prioritizing before Phase 2.
- `inviteCodes`' `allow list: if isSignedIn()` is a flagged, accepted security loosening (any
  signed-in user can list a project's active invite code if they already know its projectId) —
  revisit if this needs tightening, e.g. by restructuring `inviteCodes` as a subcollection under
  `projects/{projectId}` so `isMember(projectId)` becomes resolvable again (path-segment, not
  resource.data).

## Next
- Get a second Google account onto the device (or a second device) to test the join-by-code flow
  end-to-end.
- Prioritize Firestore emulator + rules unit tests — this exact class of bug (list-query validator
  silently rejecting rules with any unresolvable get()/exists(), even masked by OR) has now bitten
  twice and is easy to reintroduce without automated coverage.
