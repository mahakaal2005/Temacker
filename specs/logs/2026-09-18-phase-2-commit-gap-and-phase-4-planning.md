# 2026-09-18 — Phase 2 commit-gap fix + Phase 4 planning session

## Scope

User asked to plan Phase 4 fully, and first to verify Phase 2 was actually completed and committed
per the repo's usual commit pattern.

## Finding: Phase 2 marked complete in progress.md but not committed

`git status` showed the entire `feature_tasks` module (55 files), the bottom-nav shell rewrite (Home
retired, Roster/Profile relabeled Team/You), both Phase 2 cross-feature providers
(`ProjectMemberProvider`/`CurrentProjectProvider`), `AppDatabase` v3, and the architecture doc rewrite
all sitting uncommitted. Only the Firestore rules/tests (`deb8ae8`) and two device-verification
bugfixes (`f205e90`, `e8f042c`) had actually landed in git — a progress.md-vs-reality mismatch per
CLAUDE.md rule 16.

## Fix

Verified `./gradlew assembleDebug lintDebug` passed clean on the uncommitted tree, then split it into
5 atomic commits matching the repo's existing `feat:`/`docs:`/`refactor:` style:
- `96d5b24` docs: rewrite architecture doc for holder/handoff model
- `d310726` feat: feature_tasks domain + data layer (Task/Handoff, repositories, Room entities)
- `b2a86df` feat: cross-feature providers for feature_tasks (ProjectMemberProvider, CurrentProjectProvider)
- `fc55a26` feat: feature_tasks screens (Board, task detail, hand-off, incoming, decline)
- `b9a648f` refactor: bottom nav shell — Board/Team/You, retire Home

Re-ran `./gradlew assembleDebug lintDebug` against the final `HEAD` — clean.

## Phase 4 planning

Explored the current `feature_tasks` domain/data layer and `feature_project`'s Team (Roster) screen +
`Project`/`Membership`/`Role` models to scope Phase 4 ("Load/Stuck/Pulse tabs inside Team, aggregation
use cases, succession flow"). Key findings that shaped the plan:
- No cross-task aggregation query exists yet; `TaskEntity`/`HandoffEntity` already have `projectId`
  indices so new Room queries are cheap to add.
- Only `TASK_DELETED` is ever written as an `Event` today, with no Room mirror — insufficient for a
  Pulse activity feed.
- The Team/Roster screen has no tab structure; Load/Stuck/Pulse are net-new UI.
- `Project` has no cycle/versioning concept; Leader immutability is UI-only today (a string compare),
  not a domain-level guard.

Decisions confirmed with the user: events get a Room mirror + more event types written; succession
archives the old project and creates a new one that inherits the roster, scoped per-project (a user's
other project memberships are untouched); only the Leader can trigger succession; the "stuck" threshold
is a fixed 24 hours.

Full implementation plan (domain model changes, new `TeamInsightsProvider` cross-feature contract, Room
schema, Firestore rules, screens, testing, and 7 explicitly flagged architecture deviations per CLAUDE.md
rule 2) written to the plan file and approved by the user. Phase 4 code has not been written yet —
this session covered the Phase 2 commit fix and the Phase 4 design only.

## Next

Update `specs/office/phase-4-team-truth.md` with the approved design details, then begin
implementation starting with the domain model changes (§1 of the plan).
