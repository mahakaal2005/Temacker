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
- ~~A queued accept leaves the offer counted in the waiting strip and badge~~ and ~~the same answer can be queued twice~~: both fixed, see below.
- A batch write that times out in the worker may still complete from Firestore's own local queue; create/mark-done are idempotent, Pulse could get one duplicate event.
- Snackbar Undo appears only on Board.

## Follow-up verification (same day, on device)
- Snackbar Undo: tapping it removed the queued create and the queue emptied.
- Notification Accept while offline: toast "Saved on this phone. It sends when you're back online.", notification cleared; after reconnect the handoff was ACCEPTED and the task held by Rudra (DOING).
- Lock screen (adb screenshot of the keyguard): the Temacker notification shows collapsed with no task title or note, so private details stay hidden. The expanded public text was not captured (screen timed out).
- Lock screen, re-checked twice: Temacker shows only the app name and time, with no task title or note. Expanding it over adb isn't possible on this One UI (a tap drops the phone to the always-on display), so the public text is unconfirmed.
- Inbox error banner: verified on device. With the member doc deleted, Inbox showed "You don't have permission to do that." above the empty state, and Dismiss is present.
- Board previews in Android Studio: checked by the user, look good.
- Observed: WorkManager took 5-10 s to start a replay after Retry / reconnect on One UI.
- Test tasks and offers were deleted afterwards; earlier test notifications may linger in the shade.

## Incident: test-project member doc
- To trigger the banner I deleted Rudra's Leader member doc in the test project (OFGG2mNKWM7klcDCHjEy) with the user's approval. My local backup was gone by then (scratchpad cleared overnight), so the restore step failed and the doc stayed deleted until the user re-created it from a script built from values printed earlier in the session. Read back: same fields and types as the other member's doc; Board recovered. Lesson: back up and verify the restore file immediately before any destructive REST call.

## Bug found and fixed: replayed accept after a lost acknowledgement
- After the banner check the outbox held a FAILED "Accept handoff" (CONFLICT, 0 attempts) for a handoff that Firestore showed ACCEPTED. The accept had reached the server but the app never got the reply (connectivity was flapping), so the retry hit "already resolved" and was marked failed.
- Fix: on CONFLICT for accept or decline, the replayer now reads the handoff's status (new `getHandoffStatus` on the remote) and treats "already ACCEPTED/DECLINED" as done. If the lookup fails or the status differs, it stays FAILED. 3 new replayer tests; 49 total pass.
- Not reproducible on demand, so covered by unit tests only; the stale row was discarded from the queue screen.

## Two known limits fixed and verified (2026-09-21)
- Reproduced on device first: after a queued accept the Board strip, the Inbox "Waiting on you" list and the badge still counted the offer, and answering it a second time from Inbox queued a second identical row.
- Fix in `OfflineFirstTaskRepository`: an offer whose task has a PENDING queued accept/decline is filtered out of `observePendingHandoffs` (Board strip, badge) and of `observeInbox`; `queue()` skips a write when an identical PENDING one (same type and task) already exists. New DAO queries `countPendingFor` and `observeAnsweredTaskIds`. 3 new repository tests; 52 total pass.
- Verified on device with the new build: queued accept clears the strip, badge and Inbox row immediately; it sent after reconnect (handoff ACCEPTED).
- Also verified on device: the earlier "already answered counts as done" fix. With two identical queued accepts (old build), reconnecting sent the first and the second was treated as done, so no "Not sent" row.
- The duplicate guard is covered by a unit test only; the UI no longer offers a second answer, so it can't be triggered by hand.
