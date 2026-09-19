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
- **OPEN — function runtime lacks IAM:** `onHandoffWritten` fires on real handoff writes but fails
  `7 PERMISSION_DENIED` on its first Firestore read; the compute service account has no Firestore/FCM
  role. Needs `roles/datastore.user` + `roles/firebasecloudmessaging.admin` on
  `83858186212-compute@developer.gserviceaccount.com` (user to run). No push can be delivered or the
  nudge run until then.

## On-device verification (RZCWA28EAZF, signed in as Rudra Sharma, project Cycle2)

Four synthetic `[inbox-test]` tasks/handoffs were created via Firestore REST and deleted afterwards.
Confirmed: 4-tab nav; Waiting section (1 row, amber border) and Earlier section ordered by most recent
outcome (declined with reason 3h, unanswered 19h, accepted Yesterday); badge shows 1 on both selected and
unselected Inbox; tapping a Waiting row opens Incoming; Accept clears the Waiting row and the badge.
Not yet verified: tapping an Earlier row (-> task detail), empty-state and error banner on-device.

## Not done / next

- User to run the two IAM grants above, then re-test the trigger and nudge (needs a device token — Step 3).
- Artifact Registry cleanup policy for `us-central1` not set (`firebase functions:artifacts:setpolicy`).
- Step 3: FCM client, token registrar, messaging service, rationale screen. Step 4: offline outbox.
- Nothing committed yet this session.
