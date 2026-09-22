# Phase 5 — v1.0, Used by a Team That Isn't Yours

Status: complete except the project switcher, which is deferred (2026-09-22: both screens + who-set-the-role data built and verified on device)

Onboarding polish for people who join a project someone else built. Two screens plus one small data addition (who set a role).

## Screens / Flows

- **Invited member, first run** — shown once, right after a successful join by invite code (the join flow navigates to it; nothing is stored, so it never reappears on later app opens).
  Shows the project, "<Inviter> added you" (only when the inviter is known), the role, "What you can do now" (fixed: see every task and who holds it; hold a task if handed one; hand it back or decline with a reason)
  and "What needs a role" derived from the member's own permission snapshot. Primary action "Go to the board"; a plain text hint "Ask <Leader> for a role" (no button, there is no messaging).
- **What a role means** — read-only permission list (marks, not switches) for a member's role, grouped Team then Tasks. Opened by tapping a member row on Roster. Footer "Set by <name> · <date>" when known.

## Agreed decisions (2026-09-22)

- **"Needs a role" uses the app's real gating**, not the concept mock's wording: Create tasks = `assignTasks`, Delete any task = `editAnyTask`, Invite members = `manageInviteCode`,
  Change roles = `manageRoles`, Remove members = `removeMembers`. Handing on a task you hold never needs a role, so it is listed under what you can do.
- **Who set the role is stored.** `Membership` gains nullable `roleSetByUid`, `roleSetByDisplayName`, `roleSetAt` (inviter for a joiner, the acting user for a reassign, the creator for a Leader; succession copies them through).
  The invite code Firestore doc gains `createdByUid`, `createdByDisplayName` (not in the domain `InviteCode` or its Room entity); `joinProject` copies them onto the new member.
  Old members and old codes have no values; the UI then omits the line. No backfill.
- **Room v7** (`AutoMigration(6, 7)`, three nullable columns on `memberships`).
- **Firestore rules:** `members` update must set `roleSetByUid == request.auth.uid`; `inviteCodes` create must set `createdByUid == request.auth.uid`. A joiner can still write any inviter name on their own member doc (rules cannot read the code doc from there); cosmetic, accepted.
- **Nav:** two new routes in `feature_project` (`InvitedFirstRunRoute`, `RoleExplainerRoute(userId)`); cross-feature navigation stays callback-based. No new contract, no Gradle change.
- **Project switcher is out of Phase 5.** It needs its own spec and approval (see below).

## Project switcher — separate, not agreed

Today "current project" is the first project the user belongs to (`CurrentProjectProvider`), so someone in two projects cannot see the second, and a push for a project other than the current one is dropped on tap.
If taken up later it needs, before any code (CLAUDE.md rule 2): a persisted "selected project" and a switcher entry point, `CurrentProjectProvider` reading it, notification taps that switch project first (the intent already carries `projectId`),
and architecture-doc changes agreed with the user. Until then single-project behaviour is unchanged.

## Files (representative)

- `feature_project/presentation/{invited_first_run,role_explainer,role_copy}/*`
- `feature_project/data/*` (mapper, remote sources), `core/data/database/{MembershipEntity,AppDatabase}.kt`, `firestore.rules` + `firestore-tests/rules.test.js`.

## Testing

- Pure logic: `RoleCapabilitiesTest` (granted/missing lists per role, `roleSetLine` with and without data).
- Rules: reassign and invite-code create with correct/wrong/missing setter uid; join create with the new fields.
- On device: first run appears once and Back does not return to the code screen; explainer matches the Firestore permissions; old members show no "set by" line.
- ViewModel unit tests stay deferred (matches Phases 1, 2, 4).

## Out of scope for Phase 5

Project switcher, export/plan screens (Phase 6), TalkBack/contrast pass, `CreateProjectScreen` keyboard layout.

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
