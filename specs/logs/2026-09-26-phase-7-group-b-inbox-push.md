# 2026-09-26 — Phase 7 Group B: cross-project Inbox, badge, push project name

## Changed
- **Contract:** `CurrentProjectProvider.observeUserProjectRefs()` + `core/domain/model/ProjectRef`; impl in
  `ProjectCurrentProjectProvider`.
- **Badge:** `ObserveInboxBadgeCountUseCase` now sums `observePendingHandoffs` over every project (a transient error
  keeps the last value instead of dropping to zero).
- **Inbox:** new `ObserveOtherProjectsWaitingUseCase` (waiting-on-you entries per non-selected project, skipping empty
  ones and ignoring transient errors). `InboxState.otherProjects`, `InboxAction.OnOtherProjectRowClick`, a
  "Waiting in <project> · N" section per project, and two new previews. Tapping selects that project via
  `SelectedProjectStore` before navigating to the offer.
- **Push:** `projectName` added to the function payload (`functions/src/notifications.ts`, project doc read in
  `index.ts` for both the handoff trigger and the nudge job); `HandoffPush.projectName` shown as the notification
  sub-text, never on the public lock-screen version. Old pushes without the field render as before.

## Deviation from the spec
"Grouped by project" became a per-project *waiting* section only, so the existing current-project Inbox is untouched
and only actionable items from other projects are added. "Earlier" history stays current-project.

## Verified
`./gradlew assembleDebug lintDebug testDebugUnitTest` pass (new `HandoffPushTest` case); functions `npm test` 10/10
and `tsc --noEmit` clean.

## Not done yet
- **Redeploy functions** (`firebase deploy --only functions`) for the project name to appear in pushes; the app
  tolerates its absence.
- On-device check (two accounts, two projects): badge counts a handoff in the non-selected project, the "Waiting in"
  section shows it, tapping switches project and opens the offer.
