# 2026-09-17 (session 8) — On-device verification blocked: device connectivity

## Context
Per the Phase 1 completion plan, all coding was finished and committed:
- `c5d0e46` — stale-UI fix part 1 (Room reconciliation on Firestore-listener sync).
- `01101bf` — stale-UI fix part 2 (stop nested-listener churn in Roster/ManageRoles/Home/Profile
  ViewModels).
- `deb8ae8` — Firestore emulator + Jest rules test suite (45/45 passing), including a real
  Leader-self-assign rules gap found and fixed, deployed to `temacker-a0252`.

Remaining plan steps are on-device: reproduce the original stale-UI repro to confirm the fix, then
exercise Remove Member (last, per user instruction).

## Blocker
A newly connected physical device (Motorola edge 50 pro) was not detected by `adb devices` despite
being visible at the USB level (`lsusb` showed it). Diagnosed and ruled out, in order:
- Missing udev permission rule for the device's USB vendor ID (`22b8`) — added
  `/etc/udev/rules.d/51-android.rules` (`MODE="0666", GROUP="plugdev"`), reloaded udev rules; device
  re-enumerated on the bus (new bus/device number) but `adb devices` still showed nothing.
- Asked the user to confirm USB debugging is enabled in Developer Options, the USB connection mode
  is set to File Transfer (not charging-only), and to accept the on-device "Allow USB debugging?"
  authorization popup on a fresh plug-in.
- Not yet resolved. **Per user's decision, on-device verification is deferred** rather than
  continuing to debug the USB/adb connection — logged here to pick up once the device connects
  cleanly.

## Still pending (blocked on device connectivity)
1. On-device: reproduce the original stale-UI repro (role delete, permission toggle, reassign role)
   via Firestore REST ground truth, confirm the screen updates promptly with no lingering stale row.
2. On-device: Remove Member — exercise via the roster's remove action against the existing synthetic
   test member (`testMemberFake001` in project `VerifyFix`/`FCLLqo2Jx2Apdql2ijLL`), confirmed last
   per the user's instruction.
3. Clean up the leftover synthetic Firestore test data (`testRoleEditor`, `testMemberFake001`) —
   left in place for now since Remove Member's on-device exercise is what will remove the member doc
   as a side effect; cleaning it up before that test would remove the fixture needed for it.
4. Mark `specs/office/phase-1-auth-projects-roles.md`'s status complete and update `progress.md`
   accordingly, once 1–3 above are done.

## Not blocked / already complete
Everything else planned this session (Room reconciliation, listener-churn fix, rules test suite,
rules security fix + deploy) is done, built/lint-clean, and committed. Phase 1 is not being marked
complete yet — only the device-independent work is finished.
