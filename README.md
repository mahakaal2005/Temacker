# Temacker

An offline-first Android app for small teams to pass work along without losing track of it.

In Temacker, every task has exactly one holder. To move work on, the holder **hands off** the task: the other person accepts it, or declines with a reason. Each task keeps a full **baton trail**, so it's always clear who holds what, and where work is stuck.

> **Status:** In closed testing on Google Play (submitted Oct 2026). Not publicly available yet.

## Features

- **Projects and invites:** create a project, or join one with a time-limited invite code. A switcher lets you move between projects with a swipe.
- **Roles and permissions:** create custom roles with fine-grained permissions. The Leader role can't be changed, and leadership can be handed to another member.
- **Board and baton:** each task's status follows its holder history. Teammates can hand off, accept, or decline (with a reason), and ask to drop or transfer a task. Tasks can be imported from JSON.
- **Inbox and push:** an inbox shows handoffs waiting on you. Lock-screen notifications have Accept and Decline buttons, and a nudge arrives when a handoff has waited 18 hours.
- **Team insights:** see who holds the most work (Load), which handoffs are waiting too long (Stuck), and a feed of recent activity (Pulse).
- **Succession:** at the end of a cycle, the Leader archives the project and starts a new one with the same roster and roles.
- **Works offline:** changes made offline are queued and sent when the connection returns, with Retry and Discard if something conflicts.
- **Your data:** export your data as JSON or CSV, and delete your account yourself.

## Tech stack

| Layer | Tools |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3, Navigation-Compose (type-safe routes) |
| Architecture | Clean Architecture, MVI inside MVVM, use cases, Koin DI |
| Local data | Room (single source of truth), DataStore, WorkManager outbox |
| Backend | Firebase Auth (Google via Credential Manager, email), Cloud Firestore, Cloud Messaging, Cloud Functions (TypeScript), Hosting |
| Quality | JUnit 5, Turbine, AssertK, MockK, Jest + Firestore emulator for security rules, Crashlytics |

## Architecture

The app is a single `:app` module, organised by feature and then by layer:

```
com.example.temacker/
├── core/             shared Result/DataError, database, FCM, outbox, design system, cross-feature contracts
├── feature_auth/     splash and sign-in
├── feature_project/  projects, roles, members, invites, team insights, succession
├── feature_tasks/    board, handoffs, inbox, offline queue, JSON import
└── feature_profile/  profile, data export, account deletion
```

- **Dependencies point inward.** The domain layer is pure Kotlin. The data and presentation layers both depend on it.
- **Room is the source of truth.** Firestore listeners sync into Room, and the UI reads only from Room.
- **Errors are typed.** Failures come back as `Result<T, DataError>`, never as thrown exceptions.
- **Features stay separate.** They talk only through contracts in `core/domain`.

The full spec is in [`specs/ultimate_android_architecture.md`](specs/ultimate_android_architecture.md).

## Getting started

Requirements: Android Studio (recent), JDK 17+, and a Firebase project.

1. Create a Firebase project and add an Android app with the package `com.temacker.app`.
2. Put its `google-services.json` in `app/`.
3. Register your debug SHA-1 in Firebase, so Google Sign-In works.
4. Enable Authentication (Google and Email/Password), Firestore, Cloud Messaging and Functions.
5. Deploy the backend:
   ```bash
   firebase deploy --only firestore:rules,firestore:indexes,functions
   ```
6. Build:
   ```bash
   ./gradlew assembleDebug
   ```

## Testing

```bash
./gradlew testDebugUnitTest lintDebug   # Android unit tests and lint
npm install && npm run test:rules        # Firestore security-rules tests (emulator)
cd functions && npm test                 # Cloud Functions tests
```

## Project docs

- `specs/office/`: the plan for each phase, and `progress.md` for current status
- `specs/logs/`: a development log for each session
- `specs/UI/`: the design concept for every screen

## Author

Atul Kumar Singh ([@mahakaal2005](https://github.com/mahakaal2005))
