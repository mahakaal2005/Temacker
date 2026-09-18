# 2026-09-18 — Phase 4 implementation session

## Scope

Continuation of the same day's approved Phase 4 plan (see
`2026-09-18-phase-2-commit-gap-and-phase-4-planning.md`). This session implemented the full plan,
§1 through §6, across five commits.

## What landed

- `a3fe9d6` **§1 domain model** — `Project.isArchived`/`predecessorProjectId`, `Membership.isLeader`
  (denormalized, replaces the `roleName == "Leader"` string-compare in `RosterScreen`), Room
  migration to v4, and Firestore rules tightened to validate `isLeader` on member create/reassign
  (closing a self-claim-Leader gap the new field would otherwise open).
- `8573741` + `51c821a` **§2/§3 event infrastructure + `TeamInsightsProvider`** — `EventEntity`/
  `EventDao` (Room mirror, previously write-only/ephemeral), five event types now written inside
  their existing transactions/batches (task created/deleted, handoff offered/accepted/declined, task
  marked done), the new `TeamInsightsProvider` cross-feature contract (first one flowing
  feature_tasks → feature_project — reads `TaskDao`/`HandoffDao`/`EventDao` directly, a deliberate
  exception to "repository is the API surface" since the aggregation queries have no single-entity
  equivalent), and `ObserveLoadUseCase`/`ObserveStuckHandoffsUseCase`/`ObservePulseUseCase`.
- `4475788` **§4 succession use case** — `TriggerSuccessionUseCase` (Leader-only guard, then one
  Firestore `WriteBatch` via `FirestoreProjectRemoteDataSource.succeedProject()` that archives the
  old project and clones every role/membership into a new project with remapped IDs) + one Room
  `@Transaction` (`AppDatabase.withTransaction`) so the old project's archival and the new roster's
  appearance are atomic on the read side.
- `e73f0eb` **§5 succession rules** — `hasLeaderRole()` helper; `projects/{projectId}` gains a
  single-key `update` branch (Leader-only, `isArchived` `false → true` only); roles/members `create`
  gains a branch for the roster-copy, gated on a rule-validation-only `predecessorProjectId` field
  (not part of the domain models) checked against `hasLeaderRole`. This also surfaces — doesn't cause
  — the pre-existing gap that the role bootstrap rule only ever modeled the initial Leader+Default
  pair, never custom roles (already inconsistent with `CreateRoleUseCase`). 15 new rules tests added;
  68 → 78 passing.
- `b203a9c` **§6 screens** — `TeamScreen` (TabRow host: Roster/Load/Stuck/Pulse), `RosterScreen`
  refactored from a standalone `AppScaffold` owner into `RosterTabContent` (re-hosted), new
  `LoadScreen`/`StuckScreen`/`PulseScreen` (full MVI, loading/empty/error/populated previews each),
  and `SuccessionScreen` (name → confirm → submit, reached only from a Leader-gated affordance on
  `TeamScreen`; `SuccessionViewModel` independently re-verifies `isLeader` on init and bounces back
  otherwise — Compose Navigation has no native per-route auth guard, so this plus the hidden
  affordance plus the Firestore rule is the three-layer gate). `RosterRoute` renamed to `TeamRoute`
  throughout; `SuccessionRoute` added.
- Architecture doc (`specs/ultimate_android_architecture.md`) updated for all 7 deviations the plan
  flagged: the fourth cross-feature contract, the Event Room-mirror reversal, the two new `Project`
  fields, `Membership.isLeader`, and the succession rules additions.

Verification after every step: `./gradlew assembleDebug lintDebug` (clean each time, only pre-existing
Koin/AGP deprecation warnings) and `npm run test:rules` (78/78 passing after §5).

## Deliberately deferred (flagged, not silently skipped)

- **Unit tests** for the aggregation use cases/ViewModels and `TriggerSuccessionUseCase` — the plan
  raised this as an open question (Phase 1/2 deferred ViewModel unit tests by explicit instruction;
  Phase 4's own spec calls fixture-based aggregation testing in-scope) and it was never explicitly
  answered after being asked twice. Defaulted to the established precedent (rules tests + preview
  coverage only) rather than block. Revisit if that's wrong.
- **On-device golden path** — no emulator/device was available this session (`adb devices` returned
  empty). Per CLAUDE.md rule 16, `specs/office/progress.md` marks Phase 4 "implemented, pending
  on-device verification," not "complete," until someone runs the checklist there: succession as
  Leader (old project disappears, custom-role roster carries over), Load/Stuck/Pulse populated from
  real Phase 2 handoff history, non-Leader cannot see or trigger Succession.

## Notes for next session

- Phase 4 was built ahead of Phase 3 per explicit user request, despite this file's own prior note
  that it needs Phase 2/3 data to be meaningful — accepted since Load/Stuck/Pulse only actually read
  Phase 1/2's task/handoff data, not anything Phase 3 (notifications) would add.
- If the on-device pass surfaces a real bug, fix it in a follow-up commit rather than amending any of
  the five commits above (repo convention, and CLAUDE.md's git instructions).
