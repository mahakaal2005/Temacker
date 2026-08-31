# Phase 3 — Notifications, Honest Offline

Status: not started

Push notifications for handoffs, plus surfacing offline state instead of hiding it.

## Screens / Flows

- **Why notifications** — permission rationale before the OS prompt: "Temacker only pings you when a baton moves."
- **Lock screen** — dark, OS-style notification with actions sized 38dp.
- **Inbox** — 4th bottom-nav destination. Badge counts only handoffs waiting on the current user.
- **Offline** — queued-write screen surfacing WorkManager-pending/-failed writes (not silent).

## Files (representative)

- FCM setup: token registration, `feature_tasks/data/remote` push handling, notification-channel setup.
- `feature_tasks/presentation/inbox/*` (Root/Screen/ViewModel/State/Action/Event).
- WorkManager workers for retryable handoff/task writes (per architecture §7).

## Testing

- Notification fires on handoff offered/accepted/declined.
- Inbox badge count matches only handoffs where `toUid == currentUser`.
- Offline queue screen lists a write that failed while offline and clears once it syncs.

## Out of scope for Phase 3

Team analytics (Phase 4), succession (Phase 4), onboarding polish (Phase 5), export/plan screens (Phase 6).

## Completion

Mark this phase complete here, tick it off in `specs/office/progress.md`, and log the session in
`specs/logs/` (CLAUDE.md rule 4).
