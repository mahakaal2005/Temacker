# 2026-09-19 — Phase 3 Step 3: FCM client, notifications, rationale screen

## Scope

Step 3 of Phase 3. The offline outbox (Step 4) and final wrap-up (Step 5) are still open.

## What landed

- **Dependency:** `firebase-messaging` (BoM-managed).
- **Token registration:** `PushTokenRegistrar` contract (`core/domain/notification`), `FirebaseFcmTokenRegistrar`
  (`core/data/notification`) writing `users/{uid}/fcmTokens/{token}`. `App.kt` registers on every logged-in
  session (covers already-signed-in users); `OfflineFirstAuthRepository.signOut()` unregisters while still
  authenticated and deletes the FCM token. Writes are capped at 5s so an offline call never hangs sign-out.
- **Notifications (`feature_tasks/data/notification`):** `TemackerMessagingService`, `HandoffPush` (pure parser +
  copy), `HandoffNotificationFactory` (BigText, private visibility + public version, monochrome `ic_stat_baton`,
  amber accent, tagged by handoff so later pushes replace earlier ones, permission + enabled guard),
  `HandoffNotificationChannel` ("handoffs", HIGH), `HandoffActionReceiver` (inline Accept via
  `AcceptHandoffUseCase`, toast on failure).
- **Deep links:** `launchMode=singleTop`, `onNewIntent`; `MainActivity` holds a pending route and navigates only
  once the app reaches the Board (offer/recipient nudge -> Incoming, Decline action -> Decline, others -> Task
  detail). `notificationRoute` is pure.
- **Rationale screen (P3·01):** `feature_tasks/presentation/notification_rationale/*`, shown once (DataStore flag via
  `NotificationRationaleRepository`), API 33+ only, skipped if already granted; a pending notification tap takes
  priority. No State class — the screen has no state.
- **Tests:** `HandoffPushTest` (8), `NotificationRouteTest` (2); 19 unit tests pass overall.

## Verified on device (RZCWA28EAZF, Rudra's account)

Rationale -> OS prompt -> Allow -> token doc in Firestore. REST-created offers fired real FCM pushes via the
deployed function: offer, accepted ("It's theirs now."), declined (with reason), offerer nudge, recipient nudge
(19h). Nudges reached both sides and did not repeat on a second scheduler run. Accept from the shade set the
handoff to ACCEPTED, moved the holder and cleared the notification; Decline from the background opened the
Decline screen; accepted tap opened Task detail; Back returns to the Board. Cold delivery: a killed process is
woken by a push. Sign-out deleted the token doc (0 left); Google sign-in re-created it and a push arrived after.
Lock screen (notifications switched on by the user, content hidden): shows the public version "Temacker — A baton
moved." and none of the offer's text. Synthetic data deleted after each round.

## Bugs found by the verification and fixed

- **Cold-start / restored-task tap:** after the OS restored a killed app on a deeper screen, `savedInstanceState != null`
  dropped the notification intent, and navigation waited for the Board (never reached). Now each notification
  intent carries a nonce (`EXTRA_POSTED_AT`), handled once and remembered across recreation, and navigation waits
  only until the user is past Splash/Login/project setup, so warm taps from any screen work too.
- **Other-project taps:** the intent carries `projectId`; the app only shows one project (no switcher), so a tap for
  another project stays where it is instead of opening a dead screen.
- **Incoming spinner forever** when the task/handoff is gone (stale notification, deleted task): now shows
  "This handoff isn't available anymore".
- **Already-answered offers:** Incoming used to keep Accept/Decline for a handoff no longer `OFFERED` (the server
  rejects it, surfaced as a misleading conflict error). `IncomingState.unavailableNotice()` (pure, 4 tests) now
  shows "You've already accepted this" / "You declined this handoff" and hides the buttons; verified on device.

## Not verified / notes

- A tap for another project is dropped silently; a project switcher is the real fix (future phase).
- The status-bar icon is monochrome; One UI shows the app launcher icon in the shade.
