# Phase 3 — Notifications, Honest Offline

Status: in progress (Steps 0–3 done 2026-09-19: docs, backend, Inbox + badge, FCM client + rationale; Step 4 remaining)

Push notifications for handoffs, plus surfacing offline state instead of hiding it. A handoff nobody
sees is worse than no handoff, so push is load-bearing on the core mechanic. Offline never fakes a
success. Design source: `specs/UI/temacker-all-phases-android-concept.html`, P3·01–04.

## Decisions (agreed 2026-09-19)

1. **Queueable writes:** create task, offer, accept, decline, mark done. Delete stays online-only (destructive).
2. **Decline from a notification:** Accept is inline; Decline opens the Decline screen (Phase 2 requires a reason).
3. **Queue list screen:** not in the concept HTML (it shows only the offline Board: strip, "Queued" chip, Undo snackbar). Built from the existing inbox-row + strip styles.
4. **Unit tests:** new pure logic only (outbox state machine, inbox derivation, functions logic). Phase 4 tests stay deferred.
5. **Cloud Functions:** TypeScript, Admin SDK, `functions/` at repo root. FCM pushes are triggered here, never from the client.
6. **18h nudge:** goes to both sides (offerer and recipient).

## Screens / Flows

- **Why notifications (P3·01)** — permission rationale before the OS prompt: "Temacker only pings you when a baton moves." Names the three pushes (offer to you, your offer accepted/declined, your offer unanswered after 18h). Shown once, API 33+ only. "Turn on notifications" / "Not now".
- **Lock screen (P3·02)** — OS-rendered; we control content only: BigText copy, Accept/Decline actions, private visibility with a public version, monochrome small icon. Three cards: offer, accepted/declined, stale-baton nudge ("Still waiting — nobody has taken this yet").
- **Inbox (P3·03)** — 4th bottom-nav destination (Board · Inbox · Team · You). Badge counts only `OFFERED` handoffs with `toUid == me`. "Waiting on you" section, then "Earlier" (my offers unanswered ≥18h, accepted/declined outcomes with reason).
- **Offline (P3·04)** — Board offline strip, "Queued" chip on the task card, "Handoff queued… Undo" snackbar, plus a queue screen listing pending and failed writes with Retry / Discard.

## Files (representative)

- `functions/` — `onHandoffWritten` (offer → `toUid`; accept/decline → `fromUid`), scheduled nudge (>18h `OFFERED`, not yet `nudgedAt`).
- `core/domain` contracts: `PushTokenRegistrar`, `ConnectivityObserver`; impls in `core/data/notification/` and `core/data/`.
- `feature_tasks/data/notification/` — `TemackerMessagingService`, notification builder, Accept receiver.
- `feature_tasks/data/worker/` — `PendingWriteWorker` (WorkManager, `CONNECTED` constraint, deps via `KoinComponent`).
- `feature_tasks/presentation/inbox/*`, `feature_tasks/presentation/queue/*` (Root/Screen/ViewModel/State/Action/Event).
- Rationale screen (feature TBD at implementation; flag stored in DataStore).
- `AppScaffold.kt` — 4th `AppDestination`, badge wired from the app shell.
- Room v6: `PendingWriteEntity` + DAO, `@AutoMigration(5,6)`, `defaultValue` on NOT NULL columns.
- `firestore.rules` / `firestore-tests/rules.test.js` — `users/{uid}/fcmTokens/{token}` owner-only. `firestore.indexes.json` — collection-group `handoffs` (`status`, `offeredAt`).
- Dependencies (flag before sync): `firebase-messaging`, `androidx.work:work-runtime-ktx`.

## Steps

0. Docs (this file + architecture additions).
1. Backend: rules + tests, indexes, `functions/` + tests. Deploys need explicit approval; Blaze plan and Firestore region required.
2. Inbox + badge (no external dependencies).
3. FCM client, token registrar, messaging service, rationale screen.
4. Offline outbox: Room v6, worker, connectivity observer, strip / chip / snackbar / queue screen.
5. Verification on device, logs, progress tick.

## Testing

- Notification fires on handoff offered/accepted/declined.
- Inbox badge count matches only handoffs where `toUid == currentUser`.
- Offline queue screen lists a write that failed while offline and clears once it syncs; a replay rejected on validation shows as FAILED, never dropped.
- Nudge fires once per stale handoff (`nudgedAt` stamped), to both sides.
- Rules: fcmTokens readable/writable only by the owner.
- Gates after every code change: `./gradlew assembleDebug lintDebug`, `npm run test:rules`, functions tests.

## Out of scope for Phase 3

Team analytics (Phase 4), succession (Phase 4), onboarding polish (Phase 5), export/plan screens (Phase 6), Phase 4 unit tests, `CreateProjectScreen` keyboard layout.

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
