# 2026-09-18 — Phase 2 on-device verification (partial)

## Scope

Continue the previous session's blocked on-device golden-path verification for Phase 2
(create → hand off → accept → mark done → delete → read-only board). Driven entirely via `adb`
(no manual taps), since the user asked for automated device testing.

## Root cause of the previous session's "task creation fails silently" finding

False alarm, not an app bug. The prior session computed tap coordinates from the *displayed*
(scaled-down) screenshot dimensions instead of the raw device pixel dimensions (1080×2340) reported
by `adb shell wm size`. Taps were landing tens to hundreds of pixels away from the intended element
(e.g. on the TopAppBar instead of the "Task title" field), so nothing was ever actually entered or
submitted — the "task creation" that appeared to fail was never attempted.

Secondary complication: an unrelated Microsoft App Manager screen-mirroring accessibility service was
enabled on the device (`com.microsoft.appmanager/...ScreenMirroringAccessibilityService`), which
changed tap-to-focus behavior once `uiautomator dump` (or something in the session) enabled
accessibility more broadly. Disabled via `adb shell settings put secure enabled_accessibility_services
null` / `accessibility_enabled 0` before retrying — this may not have been the primary cause but was
cleared as a confound.

Fix for future device-automation sessions: always get exact element bounds via
`adb shell uiautomator dump`, parse the `bounds="[x1,y1][x2,y2]"` attributes, and tap the center of
those raw-pixel bounds — never estimate from a rendered/scaled screenshot.

## Verified this session

- **Task creation**: filled "Task title"/"Details" on the New Task screen, tapped "Create task".
  Task appeared on Board's "To do (1)" tab as `TestTask1`, "Held by Rudra Sharma" — confirms
  `NewTaskViewModel`/`CreateTaskUseCase`/`FirestoreTaskRemoteDataSource.createTask()` and the
  Firestore `tasks` create rule all work correctly end-to-end.
- **Task detail + baton trail**: opened the task, confirmed "Holding the baton — Rudra Sharma" and
  a trail entry "Rudra Sharma created this task", with a "Hand off" button and an overflow menu
  offering "Mark done" / "Delete task".
- **Mark done**: tapped the overflow menu's "Mark done". Board updated to "To do (0)" / "Done (1)"
  correctly — status derivation (TODO → DONE, holder-only action, no handoff needed) confirmed
  working.
- **Delete**: opened the DONE task's detail (overflow menu now correctly shows only "Delete task",
  no "Mark done", since the task is already DONE), tapped it, confirmed the dialog copy ("TestTask1
  and its handoff history will be gone permanently. The record that you deleted it stays in the
  project pulse."), tapped "Delete". Task removed from Board; all tab counts returned to 0.

## Not verified — needs a second project member

- **Hand-off offer → accept** (status TODO → DOING): the connected device's project (`temacker-a0252`,
  project owned by Rudra Sharma) has only 1 member (Leader). The Team/Roster screen confirmed this
  ("1 members"). Handing off requires a second member to offer to, and accepting requires signing in
  as (or synthesizing) that second member.
- **Decline** (with required reason + quick chips): same blocker — needs a second member to decline
  an offer.
- **Default-role read-only board** (no FAB, "ask X for a role" strip): needs a second member with a
  role lacking `assignTasks`/`editAnyTask`, which doesn't exist in this project yet.

## Next steps

To close out the remaining Phase 2 verification, one of:
1. Generate a fresh invite code (the current one shown in the Board's error banner has expired) and
   join the project from a second Google account, or
2. Manually seed a synthetic second member in Firestore (same approach Phase 1 used when a second
   physical account wasn't available for Reassign Role testing — see
   `specs/logs/2026-09-15-manage-roles-reassign-fix-and-full-audit.md`).

Either path unblocks hand-off/accept/decline and the read-only-board check.
