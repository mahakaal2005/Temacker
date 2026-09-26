# Phase 9 — Per-member extra permissions (deferred, not started)

Status: idea agreed 2026-09-26, spec not yet approved. Do not start before the spec is reviewed (CLAUDE.md rules 2 and 3).

## Why
Permissions belong only to roles today. Giving one person a single extra ability (e.g. Editor + Invite members) means minting a whole new role, which gets unwieldy.

## Proposed behaviour
- A member keeps their role and can additionally hold extra permissions. Effective permissions = role permissions + extras.
- Team tab: member overflow menu gets "Extra permissions", opening a sheet of the same toggles. Role-granted ones are shown locked on; only the rest can be toggled.
- Member rows show "Editor + 2 extra" so anyone can see who has more than their role.
- Add-only: extras never remove a role permission. To restrict someone, give them a different role.
- Only members who can already reassign roles may grant extras. `deleteProject` is not grantable as an extra.

## Cost (needs approval)
- New `extraPermissions` field on the membership: Room entity + auto-migration, mappers, remote data source.
- Every permission check in `firestore.rules` must consider role plus extras; new emulator tests.
- Role explainer screen ("What X can do") is already out of date (lists "Create tasks", "Delete tasks", "Change roles" instead of the real seven permissions) and should be rebuilt to show role + extras.
- Removal/leave cleanup and Cloud Functions need no change unless they read permissions (to confirm).

## Open questions
- Should a member be able to see which extras they hold?
- Should granting an extra notify the person?
