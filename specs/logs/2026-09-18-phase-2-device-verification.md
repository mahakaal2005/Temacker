# 2026-09-18 — Phase 2 on-device verification (complete)

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

## Second-member setup (session continuation)

Added a real second member via the Leader's own invite-code flow rather than a synthetic
Firestore-seeded row, so hand-off/accept/decline could be tested through the app's real code paths
end to end. This hit two real, pre-existing bugs — a stale-Room invite-code bug and a
`joinProject()` Firestore-rules gap that made joining as a genuinely new account impossible — both
found, fixed, tested (64/64 emulator tests), and deployed to production. Full details in
`specs/logs/2026-09-18-invite-code-and-join-rules-fix.md`. Second account used: `FAIQUA NAEEM`
(`faiqua.2428eee2423@kiet.edu`), joined project "VerifyFix" with the Default role (no
`assignTasks`/`editAnyTask`).

## Verified after second member joined

- **Default-role read-only board**: signed in as FAIQUA NAEEM, viewed Board — no FAB, no "Add the
  first task" CTA (both correctly hidden without `assignTasks`).
- **Hand-off offer**: as Rudra (Leader), created `HandoffTest`, tapped Hand off, selected
  FAIQUA NAEEM, sent. Baton trail immediately showed "Offered to FAIQUA NAEEM — waiting"; Rudra
  still shown as holder (correct — offer alone doesn't transfer the baton).
- **Accept**: signed in as FAIQUA NAEEM, Board showed a "1 handoff waiting on you" strip and the
  task card carried a "For you" badge — both working. Opened the incoming-handoff screen ("Rudra
  Sharma wants to hand you 'HandoffTest'"), tapped "Accept the baton". Task moved from
  To do → Doing, holder changed to FAIQUA NAEEM — confirms the TODO→DOING derivation and the
  accept-handoff Firestore transaction both work correctly end to end.
- **Decline**: as Rudra, created a second task `DeclineTest`, handed it off to FAIQUA NAEEM. Signed
  in as her, opened the incoming-handoff screen, tapped "Decline", selected the "Not my area" quick
  chip (auto-filled the reason field), tapped "Send decline". Task stayed in To do, holder remained
  Rudra Sharma (correct — decline never transfers the baton), and the baton trail recorded
  "FAIQUA NAEEM declined — Not my area".
- **Permission gating on task detail**: viewing `DeclineTest` (a task she doesn't hold, with no
  `editAnyTask`) as FAIQUA NAEEM showed zero action buttons — no Hand off, no Mark done, no Delete,
  no overflow menu options. Confirms gating is enforced per-task, not just on the Board's FAB.

## Outcome

All Phase 2 golden-path scenarios from the original plan are now verified on-device: create → hand
off → accept (TODO→DOING) and create → hand off → decline (holder unchanged), mark done
(→DONE), delete (with `editAnyTask`), and the Default-role read-only board. Phase 2 is complete
pending only the separately-tracked, deliberately-deferred unit tests.
