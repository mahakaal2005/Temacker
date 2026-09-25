# 2026-09-26 — Phase 7 Group D: create second project, archived read-only, queue in other projects

## Changed
- **#8:** `SwitchProjectScreen` gained "Create a new project" (`onNavigateToCreateProject`, wired in `ProjectGraph`);
  `CreateProjectViewModel` now takes `SelectedProjectStore` and selects the new project on success.
- **#11:** `isArchived(projectId)` in `firestore.rules`; task create/update/delete and handoff create/update require
  `!isArchived`. 5 new rules tests (110/110 total).
- **#12:** `PendingWriteDao.observeCountOutsideProject`, `PendingWriteRepository.observeCountInOtherProjects`,
  `ObserveOtherProjectQueueCountUseCase`, `BoardState.queuedInOtherProjects`, an `OtherProjectsQueueStrip` on Board
  (opens the switcher) and a preview.
- Architecture doc §8 notes the Phase 7 contract additions.

## Deviations
- No all-projects queue screen: the strip sends you to the switcher, where the existing per-project queue screen applies.
- Archived writes made from an out-of-date client or a replaying outbox now fail as permission errors and land as
  "Not sent" in the queue, where Retry/Discard already exist.

## Verified
Gradle build/lint/unit tests pass; `npm run test:rules` 110/110.

## Not done yet
Deploy rules; on-device: create a second project from the switcher and land on it; queue a write, switch projects and
see the strip; an archived project's task can't be edited.
