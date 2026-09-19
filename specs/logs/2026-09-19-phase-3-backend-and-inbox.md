# 2026-09-19 — Phase 3: spec, backend, Inbox + badge

## Scope

Phase 3 kickoff. Agreed the plan and decisions (see `specs/office/phase-3-notifications-offline.md`),
applied the approved architecture additions, then built Step 1 (backend) and Step 2 (Inbox + badge).
Steps 3 (FCM client + rationale screen) and 4 (offline outbox) are not started.

## What landed

- **Docs (Step 0):** phase-3 spec rewritten with decisions, screens, files, steps; architecture doc gained
  the FCM/Functions stack rows, package-tree notes, §7 outbox design, §8 fifth/sixth contracts
  (`PushTokenRegistrar`, `ConnectivityObserver`), Data Model entries, fcmTokens rule, testing rows.
- **Backend (Step 1):** `users/{uid}/fcmTokens/{token}` owner-only rule; `functions/` (TypeScript, Node 22,
  `us-central1` for the nam5 database) with `onHandoffWritten` (offer -> recipient, accept/decline ->
  offerer) and `nudgeStaleHandoffs` (every 30 min, >18h `OFFERED`, stamps `nudgedAt`, both sides). Pure
  logic split into `notifications.ts` (9 Jest tests). Rules tests: 79 -> 88.
- **Inbox (Step 2):** `feature_tasks/presentation/inbox/*` (Root/Screen/ViewModel/State/Action/Event/Mapper,
  5 previews), `Inbox`/`InboxEntry` domain + `ObserveInboxUseCase`, `observeInbox` in the repository
  (three syncs: pending-to-me, sent-by-me, tasks for titles), `HandoffDao.observeInbox` join. 4th
  `AppDestination.INBOX`; badge via `LocalInboxBadgeCount` fed by `InboxBadgeViewModel` at the app shell
  (counts only `OFFERED` with `toUid == me`, keyed on login state). 8 new unit tests.
- **Rules/indexes for Inbox:** handoff collection-group `list` also allows `fromUid == me`; new
  collection-group index `handoffs (projectId, fromUid)`.

## Deploys (all approved)

Rules + indexes twice (fcmTokens/nudge index, then fromUid rule/index); functions once.

## Problems found and how they were resolved

- **Functions deploy blocked on IAM (three rounds):** the CLI account is project owner but the CLI could
  not bind roles itself. User ran: `run.invoker` + `eventarc.eventReceiver` (compute SA),
  `serviceAccountTokenCreator` (Pub/Sub SA, added by the CLI on retry), and `cloudbuild.builds.builder`
  (compute SA — the build failed at the source-fetch step without it). A wrapped shell line dropped
  `--role` twice; keep gcloud commands on one line.
- **Index build lag:** the fromUid index was `CREATING` for ~3 min; the app logged FAILED_PRECONDITION on
  `FirestoreFlow` until it was `READY` (checked via the Firestore Admin REST index list).
- **Badge rendering (found on-device):** the amber badge blended into the amber selected pill and covered
  the narrow "I". Fixed with a paper ring (per the mock's box-shadow), a fixed-width glyph slot, and the
  same slot on every destination so the selected pills stay equal width.
- **Function runtime lacked IAM (resolved):** `onHandoffWritten` fired but failed `7 PERMISSION_DENIED` on its
  first Firestore read — the compute service account had no Firestore/FCM role. User granted
  `roles/datastore.user` + `roles/firebasecloudmessaging.admin`; re-test: the trigger ran clean and a manual
  run of the scheduler job (`firebase-schedule-nudgeStaleHandoffs-us-central1`) stamped `nudgedAt` on a
  19h-old synthetic offer. **Gap found:** `send.ts` swallowed send failures silently (a fake token was neither
  pruned nor logged). Fixed in code (per-failure `logger.warn`, pure `deadTokenIndexes` + test, 10 Jest
  tests) — **not yet redeployed**; a real FCM send is unverified until Step 3 registers a device token.

## On-device verification (RZCWA28EAZF, signed in as Rudra Sharma, project Cycle2)

Four synthetic `[inbox-test]` tasks/handoffs were created via Firestore REST and deleted afterwards.
Confirmed: 4-tab nav; Waiting section (1 row, amber border) and Earlier section ordered by most recent
outcome (declined with reason 3h, unanswered 19h, accepted Yesterday); badge shows 1 on both selected and
unselected Inbox; tapping a Waiting row opens Incoming; Accept clears the Waiting row and the badge.
Later the same day also confirmed: tapping an Earlier row opens task detail (baton trail shown), and the
empty state renders when nothing is waiting. Only the error banner is unverified on-device (preview only).

## Not done / next

- Redeploy functions for the send-logging fix (needs approval); verify a real FCM send once Step 3 lands.
- Artifact Registry cleanup policy for `us-central1` not set (`firebase functions:artifacts:setpolicy`).
- Step 3: FCM client, token registrar, messaging service, rationale screen. Step 4: offline outbox.
