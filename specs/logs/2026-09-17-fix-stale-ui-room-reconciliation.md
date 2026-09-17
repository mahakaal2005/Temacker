# 2026-09-17 (session 8) — Fix stale-UI-after-mutation, part 1: Room reconciliation

## Context
The stale-UI-after-mutation symptom (Roster/ManageRoles screen doesn't visually refresh after a
mutation until app restart, one observed case showed a Room write taking up to ~56s to land) was
flagged in `specs/logs/2026-09-15-manage-roles-reassign-fix-and-full-audit.md` and never
root-caused. This session, per user request, planned and started fixing Phase 1's remaining open
items fully (excluding unit tests, saved for later), with coding first and Remove Member
verification last.

## Investigation
Two research passes found two concrete, compounding bugs — not a Room/Compose issue, both ruled
out with no supporting evidence:

**Bug A — the Firestore-listener sync branch never deletes, only upserts.** Every
`OfflineFirst*Repository`'s remote-sync branch (`OfflineFirstRoleRepository.observeRoles`,
`OfflineFirstMembershipRepository.observeMembers`, `OfflineFirstInviteCodeRepository.observeActiveInviteCode`)
does `dao.upsertAll(snapshot)` on every listener event and never removes local rows no longer
present in the snapshot. A role/member deleted by any means never disappears from Room via the
listener path alone — only the mutation's own explicit `dao.delete(...)` call does that. If a
stale/lagging snapshot arrives after that explicit delete, the row is silently re-inserted and
never self-corrects. `observeMembership` (single doc) had the same gap: a `null` remote emission
(member removed) did nothing locally.

**Bug B — nested per-project listeners restart on every unrelated re-emission** (fixed separately,
see next log entry).

## Fix (this commit — Bug A only)
New Room DAO queries:
- `RoleDao.deleteMissing(projectId, keepIds)` — deletes roles for a project not in the latest
  snapshot's id set.
- `MembershipDao.deleteMissing(projectId, keepUserIds)` — same, for members.
- `InviteCodeDao.deactivateAllForProject(projectId)` — clears `isActive` for a project when the
  remote listener reports no active code.

Wired into the three repositories' remote-sync `.collect{}` blocks:
- `OfflineFirstRoleRepository.observeRoles`: `upsertAll` + `deleteMissing`.
- `OfflineFirstMembershipRepository.observeMembers`: `upsertAll` + `deleteMissing`.
- `OfflineFirstMembershipRepository.observeMembership`: on `null`, `membershipDao.delete(projectId, uid)`
  instead of a no-op — this also means "you were removed from a project" now reflects promptly on
  your own device.
- `OfflineFirstInviteCodeRepository.observeActiveInviteCode`: on `null`, `deactivateAllForProject`
  instead of a no-op.

`observeProject`/`observeUserProjects` intentionally untouched — no project-deletion feature exists
yet (per item #7's finding), so nothing to reconcile there today. Flagged, not fixed: `observeUserProjects()`'s
remote-sync branch only upserts `projectDao`, never `membershipDao` — a user added to or removed
from a project by someone else won't see their own project list update until they separately visit
that project. Related but distinct from this symptom (which is about self-initiated mutations on a
project you're already viewing); logged as a follow-up, not fixed now.

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Per CLAUDE.md rule 14, no schema/entity changes (only new DAO query methods, no new columns), no
`DataError`/`Result` changes. Bug B (listener churn) and the Firestore rules test suite land as
separate commits next.
