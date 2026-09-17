# 2026-09-18 — Invite code stale-Room bug + join-project rules gap (found via Phase 2 device testing)

## Context

While completing Phase 2's on-device golden-path verification, adding a second real project member
(needed to test hand-off/accept/decline and the read-only board) surfaced two real, pre-existing
bugs — neither introduced this session, both apparently never exercised with a genuine second
account before (Phase 1's Reassign Role testing used a synthetic Firestore-seeded member instead).

## Bug 1 — `OfflineFirstInviteCodeRepository` never deactivated stale Room rows

**Symptom:** tapping "Generate new code" on the Roster invite sheet appeared to do nothing — the
same (already-expired) code kept showing.

**Root cause:** `FirestoreInviteCodeRemoteDataSource.generateInviteCode()` correctly deactivates the
previous code in Firestore (`isActive: false`) before creating the new one. But
`OfflineFirstInviteCodeRepository.generateInviteCode()` only `upsert`ed the new code into Room —
it never called `inviteCodeDao.deactivateAllForProject()` first. The DAO's
`observeActiveForProject()` query is `SELECT * WHERE projectId = :id AND isActive = 1 LIMIT 1` with
no `ORDER BY`, so once two rows both had `isActive = 1` in Room (the stale one from before, the
fresh one just upserted), SQLite could return either — in practice it kept returning the old, wrong
one. The remote listener path (`observeActiveInviteCode` in the same repository) had the identical
gap: it only called `deactivateAllForProject` when the incoming code was `null`, never when a new
non-null active code arrived to replace a different one already cached locally.

Verified via Firestore REST that the write itself was correct (`TMK-FRL3-DY` correctly showed
`isActive: true`, the old codes correctly `isActive: false`) — the bug was purely in Room not
tracking Firestore's state.

**Fix** (`app/src/main/java/com/example/temacker/feature_project/data/repository/OfflineFirstInviteCodeRepository.kt`):
both `generateInviteCode()` and the remote-listener collect branch in `observeActiveInviteCode()`
now call `inviteCodeDao.deactivateAllForProject(projectId)` before upserting the new code, so Room's
`isActive = 1` set never has more than the one row Firestore just confirmed.

## Bug 2 — `joinProject()`'s transaction hit `PERMISSION_DENIED` for a genuinely new member

**Symptom:** signing in as a brand-new account and entering a valid invite code on the Join Project
screen failed with "You don't have permission to do that." Confirmed via logcat:
`FirebaseFirestoreException: PERMISSION_DENIED: Missing or insufficient permissions.`

**Root cause:** `FirestoreMembershipRemoteDataSource.joinProject()` runs a single Firestore
transaction that reads the invite code doc, then `projects/{projectId}`, then
`projects/{projectId}/roles/{defaultRoleId}`, then writes the new `members/{userId}` doc. The rules
for both intermediate reads were:
```
match /projects/{projectId} {
  allow read: if isMember(projectId);
  ...
  match /roles/{roleId} {
    allow read: if isMember(projectId);
```
A user joining for the first time is, by definition, not yet a member — so both reads were denied
before the transaction ever reached the `members` write. This is a genuine chicken-and-egg gap that
existed since Phase 1's rules were written; it was never caught because the only "second member"
testing done in Phase 1 (Reassign Role, Remove Member) used a member seeded directly into Firestore
with rules disabled, never an account that actually went through `joinProject()`.

**Fix** (`firestore.rules`): split `allow read` into `allow get` / `allow list` on both
`projects/{projectId}` and `projects/{projectId}/roles/{roleId}`. `get` (a single doc fetch by known
ID, which is all the join transaction ever does) is now allowed for any signed-in user — both
collections hold no sensitive data beyond name/permissions/ownerUid, the same trust boundary already
accepted for `inviteCodes`' `allow get: if isSignedIn()`. `list` (the full-collection queries Roster
and Manage Roles actually use) stays restricted to `isMember(projectId)`.

**Flagged per CLAUDE.md rule 2** — this is a real security-rule loosening, deliberate and scoped
(get-by-known-ID only, no new query surface), documented inline in `firestore.rules`.

## Testing

Added to `firestore-tests/rules.test.js` (replacing the two now-outdated "non-member cannot read"
tests that asserted the old, buggy behavior):
- `projects/{projectId}`: "signed-in non-member CAN get the project doc by known ID" (succeeds),
  "non-member cannot list/query projects" (fails).
- `projects/{projectId}/roles/{roleId}`: same pattern — `get` succeeds, `list` fails.

`npm run test:rules` — 64/64 passing (was 62/62 before these two replaced tests; net +2 after
removing 2 outdated + adding 4 new, minus overlap).

## Deployment

Both fixes required together to unblock join — deployed via `firebase deploy --only firestore:rules`
to `temacker-a0252` after explicit user confirmation. Verified immediately after deploy: the same
join-by-invite-code flow that previously failed with `PERMISSION_DENIED` succeeded, and the new
member (`FAIQUA NAEEM`) appeared correctly in the Roster with the Default role.

## Verification

After both fixes, the full Phase 2 golden path (create → hand off → accept, and separately
create → hand off → decline) was exercised end-to-end with two real signed-in accounts — see
`specs/logs/2026-09-18-phase-2-device-verification.md`.
