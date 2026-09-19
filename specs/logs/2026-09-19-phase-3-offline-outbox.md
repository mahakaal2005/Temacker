# 2026-09-19 — Phase 3 Step 4: offline outbox

## Built
- `ConnectivityObserver` (contract `core/domain/network`, impl `core/data/network`, validated-internet callback) + `ACCESS_NETWORK_STATE`.
- Room v6: `pending_writes` table (`PendingWriteEntity`/`Dao`), `AutoMigration(5,6)`, `6.json` committed. New table, so no `defaultValue` columns were needed.
- Repository routing (`OfflineFirstTaskRepository`): create, offer, accept, decline, mark done. Offline (connectivity check first, because batch commits hang offline) or a `NO_INTERNET` result writes an outbox row and enqueues the worker; signatures unchanged, so use cases and ViewModels are untouched. Delete stays online-only.
- Queued create carries a client-generated id (`taskId` param added to the remote `createTask`), so a replay overwrites the same doc.
- `PendingWriteReplayer` + `PendingWriteWorker` + `PendingWriteScheduler` (`feature_tasks/data/worker`). Oldest-first replay through the remote, mirrored to Room. `replayVerdict`: WAIT (offline, no attempt spent) / RETRY (server errors, cap 5) / FAIL (conflict, permission). FAIL keeps the row as FAILED with the DataError name; never dropped.
- Notification Accept while offline queues and shows a "Saved on this phone" toast.
- Board: offline/queued/failed strip, Queued / Not sent chip, queued creates overlaid as cards (not written to the mirrored tasks table), "Queued. Undo" snackbar (new `snackbarHost` slot on `AppScaffold`, flagged in the plan).
- Queue screen (`presentation/queue/*`): Not sent / Waiting to send sections, plain-language reasons, Retry (failed only) and Discard.
- 22 new unit tests (verdict, payload mapper, replayer, board overlay, queue mapper); 46 total pass.

## Verified on device (RZCWA28EAZF, wifi + data off via adb)
- Offline create shows strip, Queued chip, Undo snackbar; offline mark done doesn't hang. Wifi on: both drained, Firestore shows the new task and DONE.
- Forced conflict (accept queued offline, offer declined via REST, wifi on): row FAILED, strip "1 couldn't be sent", "It changed before this could send". Retry goes PENDING then FAILED again (~5-10 s of WorkManager latency); Discard clears it.
- Test data removed afterwards; "do it" restored to TODO. Pulse still has the test events.

## Known limits
- A queued accept leaves the offer counted in the "waiting on you" strip and Inbox badge until it sends.
- Queuing the same answer twice is not deduped; the second replay fails as a conflict.
- A batch write that times out in the worker may still complete from Firestore's own local queue; create/mark-done are idempotent, Pulse could get one duplicate event.
- Snackbar Undo appears only on Board.

## Follow-up verification (same day, on device)
- Snackbar Undo: tapping it removed the queued create and the queue emptied.
- Notification Accept while offline: toast "Saved on this phone. It sends when you're back online.", notification cleared; after reconnect the handoff was ACCEPTED and the task held by Rudra (DOING).
- Lock screen (adb screenshot of the keyguard): the Temacker notification shows collapsed with no task title or note, so private details stay hidden. The expanded public text was not captured (screen timed out).
- Still unverified: Inbox error banner (needs a real listener error; preview only), and the Board previews in Android Studio.
- Observed: WorkManager took 5-10 s to start a replay after Retry / reconnect on One UI.
- Test tasks and offers were deleted afterwards; earlier test notifications may linger in the shade.
