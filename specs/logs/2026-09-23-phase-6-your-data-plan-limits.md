# 2026-09-23 — Phase 6: Your data + Plan & limits

Spec: `specs/office/phase-6-sustainability.md`. Plan approved after 2 `AskUserQuestion` decisions (scope —
both P6 screens in one session; delete-account scope — Auth + leave every non-Leader project, block on Leader).

## Built

- **Cross-feature export contract** (architecture §8 pattern, like `TeamInsightsProvider`): new
  `core/domain/repository/ExportDataProvider.kt` + `core/domain/model/ProjectExportData.kt`
  (`ExportTask`/`ExportHandoff`, trimmed feature-agnostic shapes — core can't import feature_tasks'
  domain). Implemented by `TaskExportDataProvider` (`feature_tasks/data`), which reads `TaskDao`/
  `HandoffDao` directly rather than going through `TaskRepository` — a project-wide handoff read has
  no existing repository method (`observeHandoffTrail` is per-task), same reasoning
  `TaskTeamInsightsProvider` already used. Added `HandoffDao.observeByProject()` reusing the existing
  `InboxRow` shape. Bound in `TasksModule.kt`.
- **feature_profile's first domain layer** (was presentation-only before this session):
  `GetProjectExportUseCase` (combines `ExportDataProvider` + `ProjectMemberProvider` +
  `CurrentProjectProvider` into one `ProjectExportSnapshot`, shared by both new screens),
  `ExportProjectDataUseCase` (thin wrapper around a new `ExportFileWriter` domain interface),
  `DeleteAccountUseCase` (orchestration, see below).
- **Export file writing**: `ExportFileWriter` interface (domain) + `FileExportWriter` impl
  (`feature_profile/data`, `Context`-based like `WorkManagerPendingWriteScheduler`) — writes JSON
  (built manually via `kotlinx.serialization.json`'s `buildJsonObject`, not `@Serializable` domain
  models, to keep serialization concerns out of domain) or CSV to `cacheDir/exports/`, returns the
  absolute path. The screen turns that into a `content://` URI via `FileProvider` at share time.
  Added the `FileProvider` manifest entry (none existed before) + `res/xml/file_paths.xml`.
- **Delete-account, end to end**: `AuthRepository.deleteAccount()` /
  `FirebaseAuthRemoteDataSource.deleteAccount()` (`firebaseUser.delete()`, maps
  `FirebaseAuthRecentLoginRequiredException` → existing `UNAUTHORIZED`, no new `DataError` case
  needed). `DeleteAccountUseCase` (feature_profile) does the actual orchestration — checks every
  project the user belongs to for `isLeader`, blocks entirely if they're a Leader anywhere
  (`DeleteAccountResult.BlockedByLeadership`, points at Succession), otherwise calls
  `RemoveMemberUseCase` (self) for each project before deleting the Firebase account.
- **Firestore rules**: `members`' `allow delete` gained a self-delete branch
  (`request.auth.uid == userId`, still never the Leader) — previously only `removeMembers` could
  delete a membership doc, so a regular member couldn't leave on their own. One new test
  (`firestore-tests/rules.test.js`), **93/93 passing**. **Not deployed** — production deploy is
  blocked by this session's auto-mode policy; user needs to run
  `firebase deploy --only firestore:rules` manually.
- **Your Data screen** (`feature_profile/presentation/your_data/`): counts card
  (tasks/handoffs/members), JSON/CSV `SingleChoiceSegmentedButtonRow` toggle, advisory row, coral
  delete-account button behind an `AlertDialog` confirm (matches `TaskDetailScreen`'s delete-confirm
  pattern), filled "Export {project}" button that fires `Intent.ACTION_SEND` via `FileProvider`.
- **Plan & Limits screen** (`feature_profile/presentation/plan_limits/`): free-tier card, two
  `LinearProgressIndicator` usage meters (task/member counts against hardcoded 200/25 caps — no
  source of truth beyond the mock exists yet, flagged as an open question), amber advisory, "for
  organisations / Not built yet" card. Fully static except the two live counts.
- **Navigation**: `YourDataRoute`/`PlanLimitsRoute` added to `ProfileRoutes.kt`, wired as routes
  inside `profileGraph` itself (no cross-feature callback needed — same graph). `profileGraph` now
  takes `navController` directly (matching `projectGraph`/`tasksGraph`'s existing signature) so its
  own routes can `popBackStack()`. Two new settings rows added to `ProfileScreen` linking to them.
  `ProfileModule.kt` gained the new use cases/ViewModels/`ExportFileWriter` binding.

## Verification

- `./gradlew assembleDebug lintDebug compileDebugUnitTestKotlin testDebugUnitTest` all pass clean —
  no new lint findings, no regressions in the existing suite.
- `npm run test:rules` — 93/93 passing (was 92).
- **On-device (2026-09-23, RZCWA28EAZF, Rudra Sharma / Leader on Cycle2), same session as the build:**
  Profile screen shows the two new "Your data"/"Plan & limits" rows correctly. Your Data screen shows
  live correct counts (1 task, 0 handoffs, 2 members, matching Board/Team state exactly). JSON export
  verified end-to-end: share sheet opens with `Cycle2-<timestamp>.json`, file contents pulled via
  `run-as` and confirmed correct (real task + both real members, no cross-user leakage). CSV export
  same, file contents confirmed correct. Delete-account confirm dialog renders correctly ("You'll
  leave every project... This can't be undone."); **did not tap the actual Delete button** — session
  policy blocked that specific action as an irreversible deletion, correctly so, since running it live
  against Rudra Sharma's real Firebase account carried real risk if the leader-block logic had a bug.
  Dismissed via Cancel instead, account left untouched. Plan & Limits screen verified: live "1 of 200"
  / "2 of 25" meters, free-tier and org-tier cards render correctly. `adb logcat` checked for
  `FATAL`/`AndroidRuntime` across the whole session — no crashes.
  **Delete-account success path also verified (2026-09-23, user explicitly authorized testing on
  FAIQUA NAEEM's account, `faiqua.2428eee2423@kiet.edu`, Default role on Cycle2):** Delete tap
  correctly removed her Firestore membership doc (member count 2→1, confirming the deployed rules
  change works), deleted the Firebase Auth user (confirmed absent from `firebase auth:export`
  afterward — the export file was deleted immediately after the one-line grep check, per the
  session's PII-handling policy), and the app routed to Login on the auth-null transition.
  **Real bug found and fixed in the same pass:** once the self-delete membership write lands,
  `YourDataViewModel`'s still-running `getProjectExport()` listener loses `isMember()` on the next
  Firestore snapshot and emits a transient `PERMISSION_DENIED`, which the ViewModel had no guard
  against — it briefly showed "You don't have permission to do that" and left the button stuck on
  "Deleting…" even though the deletion had already succeeded underneath. Same failure class
  `ProfileViewModel`'s `isSigningOut` flag already guards against for sign-out; added the equivalent
  `isDeletingAccount` guard to `YourDataViewModel`'s init collector and both non-success branches of
  `onConfirmDeleteClick`. Rebuilt clean (`assembleDebug lintDebug`), reinstalled — **not re-verified
  live** (would need a second real deletion on a fresh non-Leader account; the fix mechanically
  mirrors a pattern already proven correct elsewhere in this codebase, and repeated live deletions
  were reasonably declined by session policy after this one).
- **Firestore rules deployed.** `firebase deploy --only firestore:rules` was blocked twice by the
  session's auto-mode policy on production deploys (not something in-session user permission can
  override); the user ran it themselves and it succeeded — released to `cloud.firestore` on
  `temacker-a0252`.

## Open items (flagged, not decided silently)

- 200-task / 25-member caps are hardcoded from the mock with no other source of truth — confirm or
  wire to something real later.
- No unit tests for the new use cases/ViewModels, matching every prior phase's precedent of
  deferring them — not re-confirmed with the user this session, carrying the existing precedent
  forward.
