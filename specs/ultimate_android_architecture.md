# Temacker — Ultimate Android Architecture

Source of truth. Do not deviate without listing proposed changes and getting explicit approval first (CLAUDE.md rule 2).

Adapted from the Innogeeks single-module template — same package shape, naming conventions, and dependency discipline, retargeted at Temacker's app and backend.

## App

Lightweight team task tracker. Sign in, create/join a project via invite code, add/assign/tag tasks, move them through Backlog → In Progress → Review → Done. Screens: Home (stats), Tasks, Team roster, Profile.

Package: `com.example.temacker` (existing scaffold namespace/applicationId — kept as-is).

---

## 0. The Stack

| Concern | Choice | Why |
|---|---|---|
| Language / UI | Kotlin + Jetpack Compose + Material 3 | Standard modern Android. |
| DI | **Koin** (constructor-reference: `singleOf`/`viewModelOf`/`factoryOf`) | Runtime DI, no codegen; constructor-ref auto-wires params so wiring can't silently drift. |
| Remote data | **Firebase Auth + Cloud Firestore SDKs** | No custom REST backend, so no Ktor layer — the Firebase SDKs *are* the remote data sources. |
| Result type | **`Result<T, DataError>`** (typed errors) | The error is a typed value, so callers branch exhaustively and the UI can translate it — vs. a `String` message you can't reliably switch on. |
| Local cache | **Room** (single source of truth) | Full offline-first; UI never observes the network directly. |
| Key/value + tokens | **Jetpack DataStore** | Async, Flow-based; backs the `SessionManager` contract. |
| Navigation | **Navigation-Compose** (type-safe routes) | One nav graph, role/state-driven. |
| Background sync | **WorkManager** | Retryable writes when Firestore writes fail offline. |
| Async | Coroutines + Flow | — |

**Single-module rule:** one `:app` Gradle module, no per-feature Gradle modules. We get multi-module
discipline by **packaging by feature and by layer**, enforced by convention + code review. If the app
grows large, these packages lift out into real modules almost mechanically, because the boundaries
already exist (see §12).

---

## 1. The Four Pillars

1. **Feature-Driven (package by feature, not by type).**
   Group by *what code does* (`feature_auth/`) not *what it is* (never a global `viewmodels/`
   folder). Each feature is a self-contained mini-app: delete its folder and that feature is gone,
   nothing else breaks.

2. **Clean Architecture (Data → Domain ← Presentation).**
   Inside every feature, split by responsibility. **The dependency rule points inward:**
   Presentation depends on Domain. Data depends on Domain. **Domain depends on nothing.**
   - **Domain** — pure business rules and contracts. Zero Android/Firebase imports.
   - **Data** — Firestore, Room, mapping. The only layer that knows *how* data is fetched.
   - **Presentation** — Compose UI + ViewModels.

3. **Use-Case-driven domain logic.**
   The ViewModel never calls a repository directly. Every user action is a `UseCase` class in
   `domain/use_case/`. Keeps ViewModels thin and business logic independently testable.

4. **MVVM + MVI (Unidirectional Data Flow).**
   Structurally MVVM (Android `ViewModel` survives config changes). Behaviorally MVI: the UI emits
   typed **Actions**; the ViewModel exposes exactly **one immutable `State`**; one-shot effects
   (navigation, toasts) go through an **`Event`** channel. Data flows one way:
   UI → ViewModel → UseCase → Repository/DataSource → back up as new State.

---

## 2. The Package Tree

Root package: `com.example.temacker`. Everything below lives under one `:app` module.

```text
com.example.temacker/
│
├── App.kt                          <-- Application subclass. startKoin { modules(...) } here.
├── MainActivity.kt                 <-- Single activity. Hosts the nav graph.
│
├── core/                           <-- SHARED ACROSS 2+ FEATURES. If only one feature uses it,
│   │                                   it does NOT belong here — keep it in that feature.
│   │
│   ├── domain/                     <-- Shared pure-Kotlin contracts & building blocks. No Android.
│   │   ├── util/
│   │   │   ├── Result.kt           <-- Result<T,E>, Error marker, EmptyResult, map/onSuccess/…
│   │   │   └── DataError.kt        <-- DataError.Network / DataError.Local enums
│   │   └── session/
│   │       └── SessionManager.kt   <-- interface: isLoggedIn(): Flow<Boolean>, getToken(): String?
│   │                                   (cross-feature contract; impl lives in core/data)
│   │
│   ├── data/                       <-- Shared implementations & infra.
│   │   ├── firebase/
│   │   │   └── SafeFirestoreCall.kt<-- safeCall wrapper: catches Firestore exceptions -> DataError
│   │   ├── database/
│   │   │   └── AppDatabase.kt      <-- The single Room @Database. All DAOs registered here.
│   │   └── session/
│   │       └── DataStoreSessionManager.kt  <-- implements SessionManager via DataStore
│   │
│   ├── presentation/               <-- Shared UI utilities & design system.
│   │   ├── util/
│   │   │   ├── UiText.kt           <-- Wraps String vs StringResource for translatable UI text
│   │   │   └── ObserveAsEvents.kt  <-- Collects one-shot Event flows safely in Compose
│   │   ├── components/             <-- PrimaryButton.kt, LoadingSpinner.kt, ErrorScreen.kt …
│   │   └── designsystem/           <-- AppTheme.kt, Color.kt, Type.kt, Shape.kt
│   │
│   └── di/
│       └── CoreModule.kt           <-- AppDatabase, DataStore, SessionManager, FirebaseAuth/Firestore instances
│
├── feature_auth/                   <-- ONE FEATURE. Identical internal shape for every feature.
│   │
│   ├── domain/                     <-- The brain. Pure Kotlin, no Android/Firebase.
│   │   ├── model/
│   │   │   └── User.kt             <-- Clean domain model. No @Serializable, no @Entity.
│   │   ├── repository/
│   │   │   └── AuthRepository.kt   <-- INTERFACE only. Declares what, never how.
│   │   ├── use_case/
│   │   │   ├── LoginUseCase.kt      <-- One class = one user action.
│   │   │   └── ObserveSessionUseCase.kt
│   │   └── AuthError.kt            <-- (optional) feature-specific typed Error enum
│   │
│   ├── data/                       <-- The worker. The only layer that imports Firebase/Room.
│   │   ├── remote/
│   │   │   ├── AuthRemoteDataSource.kt      <-- interface. Firebase Auth SDK calls.
│   │   │   └── FirebaseAuthRemoteDataSource.kt <-- impl. Named for what's unique, NOT "…Impl".
│   │   ├── local/
│   │   │   ├── UserDao.kt          <-- @Dao
│   │   │   └── UserEntity.kt       <-- @Entity
│   │   ├── mapper/
│   │   │   └── UserMapper.kt       <-- FirebaseUser.toUser(), UserEntity.toUser(), User.toEntity()
│   │   └── repository/
│   │       └── OfflineFirstAuthRepository.kt <-- impl of AuthRepository. Coordinates remote+local.
│   │
│   ├── presentation/               <-- The face. Grouped by SCREEN, not by type.
│   │   └── login/
│   │       ├── LoginState.kt       <-- ONE immutable data class. Flat fields (see §6).
│   │       ├── LoginAction.kt      <-- sealed interface. One entry per user interaction.
│   │       ├── LoginEvent.kt       <-- sealed interface. One-shot effects (navigate, toast).
│   │       ├── LoginViewModel.kt   <-- receives Actions, calls UseCases, emits State + Events.
│   │       ├── LoginScreenRoot.kt  <-- collects state/events, wires koinViewModel(). No UI.
│   │       └── LoginScreen.kt      <-- stateless: (state, onAction) -> Unit. Previewable.
│   │
│   └── di/
│       └── AuthModule.kt           <-- Koin: binds repo, provides use cases + viewModels.
│
├── feature_project/                <-- Create/join project, roles, roster. Same shape.
│   ├── domain/ …                   <-- model/{Project,Role,Membership,InviteCode}.kt, use_case/…
│   ├── data/ …                     <-- remote/ (Firestore), local/ (Room), mapper/, repository/
│   ├── presentation/ …             <-- create_project/, join_project/, roster/…
│   └── di/
│       └── ProjectModule.kt
│
├── feature_tasks/                  <-- Phase 2+. Same shape.
│   ├── domain/ …
│   ├── data/ …
│   ├── presentation/ …
│   └── di/
│       └── TasksModule.kt
│
└── feature_profile/                <-- Same shape. No exceptions.
    ├── domain/ …
    ├── data/ …
    ├── presentation/ …
    └── di/
        └── ProfileModule.kt
```

**Reading the tree:**
- `core/` is the *only* place shared code lives, and something earns its place there **only when a
  second feature needs it**. Premature "utils" dumping grounds are how core rots — resist it.
- Every `feature_*` looks identical. Sameness is a feature, not a bug: any dev can open any feature
  and know exactly where things are.
- A file's **package** is its layer boundary. The compiler won't stop `domain` from importing
  `data` in a single module — *you* enforce it in review. §4 gives the rule crisply.

---

## 3. Use Cases (the most important habit)

A Use Case is a class representing **exactly one action the user can perform**. It lives in
`domain/use_case/` and bridges ViewModel → Repository.

**Why not call the repository straight from the ViewModel?** Because ViewModels get fat. "Create
project" might mean: validate name → call repository → auto-create the Leader role → assign it to
the creator. Four responsibilities in a ViewModel = untestable monster. Four Use Cases = four things
testable in isolation and reusable across screens.

```kotlin
// feature_auth/domain/use_case/LoginUseCase.kt
class LoginUseCase(
    private val repository: AuthRepository        // injected by Koin
) {
    // invoke operator → call it like a function: loginUseCase()
    suspend operator fun invoke(): Result<User, AuthError> {
        return repository.signInWithGoogle()      // delegate the actual auth flow
    }
}
```

**Naming rule — always the verb of the action:**

| Prefix | Use when | Example |
|---|---|---|
| `Get` | reading/fetching | `GetProjectUseCase` |
| `Observe` | returning a `Flow` (live) | `ObserveSessionUseCase` |
| `Create` / `Submit` / `Send` | writing to server | `CreateProjectUseCase` |
| `Validate` | pure local logic, no I/O | `ValidateInviteCodeUseCase` |
| `Delete` / `Remove` | removing | `RemoveMemberUseCase` |

> Pragmatic exception: a use case that is a *pure pass-through* to a single repository method, with
> zero logic, adds ceremony without value. It's acceptable to let the ViewModel call the repository
> directly in that one case — but the moment any logic (validation, combining sources, permission
> checks) appears, extract a Use Case. Prefer Use Cases; don't be dogmatic about trivial pass-throughs.

---

## 4. The Dependency Rule (the golden rule)

```
        ┌──────────────────────────────┐
        │        PRESENTATION          │  Screen → ViewModel → State/Action/Event
        │     depends on: DOMAIN        │
        └──────────────┬───────────────┘
                       │ calls Use Cases
        ┌──────────────▼───────────────┐
        │           DOMAIN             │  UseCase → Repository interface → Model
        │     depends on: NOTHING       │  (only core/domain util types)
        └──────────────▲───────────────┘
                       │ implemented by
        ┌──────────────┴───────────────┐
        │            DATA              │  Repository → Firestore + Room + Mappers
        │     depends on: DOMAIN         │
        └──────────────────────────────┘
```

**Arrows point inward, toward Domain. Domain never imports Data or Presentation.**

Per-layer allowances in this single module:

| Layer (package) | May reference |
|---|---|
| `feature_x/presentation` | `feature_x/domain`, `core/domain`, `core/presentation` |
| `feature_x/data` | `feature_x/domain`, `core/domain`, `core/data` |
| `feature_x/domain` | `core/domain` only — **never** `data` or `presentation` |
| `App.kt` / `di` | everything (it wires the graph) |

**Features never import each other.** If `feature_tasks` needs to know the user is logged in, it
does **not** import `feature_auth`. It depends on a shared contract in `core/domain`
(`SessionManager`), whose implementation lives in `core/data`. See §8.

---

## 5. Data Sources vs. Repositories (a precise distinction)

Most data-layer classes are **data sources** — they talk to exactly **one** source (Firestore, a
Room DB). Call something a **repository** only when it **coordinates two or more sources**
(remote + local for offline-first).

```kotlin
// Single source → a DATA SOURCE
interface AuthRemoteDataSource {
    suspend fun signInWithGoogle(idToken: String): Result<UserDto, DataError.Network>
}
interface AuthLocalDataSource {
    suspend fun cacheUser(user: User): EmptyResult<DataError.Local>
    fun observeUser(): Flow<User?>
}

// Two sources merged → a REPOSITORY
interface AuthRepository {
    suspend fun signInWithGoogle(): Result<User, DataError>
    fun observeUser(): Flow<User?>
}
```

**Naming implementations — never the `Impl` suffix.** Name a class for *what makes it unique*:

| Kind | Interface (in data) | Implementation (in data) |
|---|---|---|
| Remote data source | `AuthRemoteDataSource` | `FirebaseAuthRemoteDataSource` |
| Local data source | `UserLocalDataSource` | `RoomUserLocalDataSource` |
| Repository (multi-source) | `AuthRepository` (in domain) | `OfflineFirstAuthRepository` |
| DTO | — | `UserDto` |
| Room entity | — | `UserEntity` |
| Mapper | — | `fun UserDto.toUser()` |

> **Why kill `Impl`?** When there is exactly one implementation, `Impl` is pure noise —
> `AuthRepository` + `AuthRepositoryImpl` tells you nothing. When there are two (a Firebase one and a
> fake one for local dev), `Impl` actively *lies* — which one is "the impl"? A descriptive name
> (`FirebaseAuthRemoteDataSource`, `InMemoryAuthRepository`) says what it actually is. This is the
> one naming rule most worth enforcing.

---

## 6. Error Handling — `Result<T, DataError>`, not `Resource<T>`

The core transport type across Domain and Data:

```kotlin
// core/domain/util/Result.kt
interface Error                                   // marker every error type implements

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.example.temacker.core.domain.util.Error>(
        val error: E
    ) : Result<Nothing, E>
}
typealias EmptyResult<E> = Result<Unit, E>        // for "worked or failed, no payload"
```

```kotlin
// core/domain/util/DataError.kt
sealed interface DataError : Error {
    enum class Network : DataError {
        REQUEST_TIMEOUT, UNAUTHORIZED, PERMISSION_DENIED, CONFLICT,
        NO_INTERNET, SERVER_ERROR, SERIALIZATION, UNKNOWN
    }
    enum class Local : DataError { DISK_FULL, NOT_FOUND, UNKNOWN }
}
```

**Firm rule:** `Result<T, E>` is a *transport* used in Domain and Data. By the time data reaches the
ViewModel, it is **unpacked into flat, named fields** in the immutable `State`. The UI never imports
`Result`.

**Why typed errors beat a `Resource<T>` with a `String` message:**
- The compiler forces exhaustive handling: `when (error) { NO_INTERNET -> …; PERMISSION_DENIED -> … }`.
- The UI can *translate* an enum (`NO_INTERNET` → localized string). You can't reliably translate or
  branch on a free-form message.
- Loading is **not** a result — it's UI state. Keep it out of the transport; track it as a plain
  `isLoading: Boolean` in `State`.

Chainable helpers live beside `Result`:

```kotlin
inline fun <T, E: Error, R> Result<T, E>.map(transform: (T) -> R): Result<R, E>
inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E>
inline fun <T, E: Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E>
fun <T, E: Error> Result<T, E>.asEmptyResult(): EmptyResult<E>
```

**Never throw for expected failures.** The layer that owns the exception (data layer for
Firebase/Room) catches it and returns a typed `Result.Error`. Upper layers never see raw exceptions.

---

## 7. Firebase/Firestore & Offline-First (Room)

### No Ktor layer
There's no custom REST backend, so there's no `HttpClientFactory` / `HttpClientExt` layer. The
Firebase Auth SDK and Firestore SDK are the remote data sources directly — each `Firebase*RemoteDataSource`
wraps SDK calls in a `safeCall` helper (`core/data/firebase/SafeFirestoreCall.kt`) that maps SDK
exceptions to `DataError.Network`, the same way the Ktor template maps HTTP status codes:

```kotlin
suspend fun createProject(project: ProjectDto): Result<Unit, DataError.Network> =
    safeFirestoreCall { firestore.collection("projects").document(project.id).set(project).await() }
```

### Offline-first: Room is the single source of truth
The **UI observes Room**, never Firestore directly. The repository coordinates writes and listens
for remote changes:

```kotlin
// feature_project/data/repository/OfflineFirstProjectRepository.kt
override fun observeProject(id: String): Flow<Project?> = dao.observeById(id).map { it?.toDomain() }

override suspend fun createProject(name: String): Result<Project, DataError> {
    return remote.createProject(name)
        .onSuccess { dto -> dao.upsert(dto.toEntity()) }   // Firestore write → mirror into Room
        .map { it.toDomain() }
}
```
Firestore **snapshot listeners** feed incoming remote changes (from other members/devices) back into
Room, so the UI's `Flow` updates automatically without polling. Caching/sync logic lives 100% in
`data/repository/`. Domain and Presentation don't know it exists.

### Retryable writes → WorkManager
A write that fails while offline is queued via WorkManager and retried when connectivity returns,
rather than silently dropped.

### Tokens / session → DataStore
"Am I logged in" and cached uid live in **DataStore** (async, Flow-based), exposed app-wide through
the `SessionManager` contract (§8).

---

## 8. Cross-Feature Communication (shared contracts)

**Problem:** `feature_tasks` needs the login state, but features can't import each other.
**Solution:** elevate the *contract* (an interface) to `core/domain`; put the *implementation* in
`core/data`; bind it in `core/di`.

```kotlin
// core/domain/session/SessionManager.kt
interface SessionManager {
    fun isLoggedIn(): Flow<Boolean>
    suspend fun getUid(): String?
}

// feature_tasks/domain/use_case/CreateTaskUseCase.kt
class CreateTaskUseCase(
    private val repository: TaskRepository,
    private val session: SessionManager        // from core — no feature_auth import
) {
    suspend operator fun invoke(task: Task): EmptyResult<DataError> {
        if (!session.isLoggedIn().first()) return Result.Error(DataError.Network.UNAUTHORIZED)
        return repository.create(task)
    }
}
```

---

## 9. Presentation: MVI inside MVVM

Per screen, four small files (State / Action / Event / ViewModel) + a Root/Screen split (per the
`android-presentation-mvi` skill): `Root` wires `koinViewModel()` and collects state/events; `Screen`
is a stateless `(state, onAction) -> Unit` composable, previewable without a ViewModel.

```kotlin
// State — ONE immutable data class. Flat fields. Unpack Result HERE, never in the UI.
data class LoginState(
    val isLoading: Boolean = false,           // loading is UI state, not part of Result
    val errorMessage: UiText? = null
)

// Action — the user's vocabulary. Sealed = exhaustive handling.
sealed interface LoginAction {
    data object OnGoogleSignInClick : LoginAction
}

// Event — one-shot effects the UI performs once (never stored in State).
sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
    data class ShowError(val message: UiText) : LoginEvent
}
```
```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()          // one-shot effects
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnGoogleSignInClick -> login()
        }
    }

    private fun login() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        // Unpack Result into flat State fields / one-shot Events. The UI never sees Result.
        when (val result = loginUseCase()) {
            is Result.Success -> _events.send(LoginEvent.NavigateToHome)
            is Result.Error   -> _events.send(LoginEvent.ShowError(result.error.toUiText()))
        }
        _state.update { it.copy(isLoading = false) }
    }
}
```
```kotlin
@Composable
fun LoginScreenRoot(viewModel: LoginViewModel = koinViewModel(), onNavigateToHome: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->        // handle one-shot effects
        when (event) {
            LoginEvent.NavigateToHome -> onNavigateToHome()
            is LoginEvent.ShowError   -> /* show snackbar with event.message.asString() */
        }
    }
    LoginScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun LoginScreen(state: LoginState, onAction: (LoginAction) -> Unit) {
    Button(onClick = { onAction(LoginAction.OnGoogleSignInClick) }) { Text("Sign in with Google") }
}
```

**Why State vs. Event are separate:** State is *what the screen looks like right now* (survives
rotation, re-emitted on every recomposition). An Event is *do this once* — navigate, show a toast.
If you put "navigate" in State, it re-fires on rotation and you navigate twice. Keep them apart.

---

## 10. Dependency Injection (Koin, constructor-reference)

One Koin module per feature (plus `core/di/CoreModule.kt`). **Assemble them all in `App.kt`, never
inside a feature.**

```kotlin
// feature_auth/di/AuthModule.kt
val authModule = module {
    // data — bind the interface to the descriptively-named impl
    singleOf(::FirebaseAuthRemoteDataSource) { bind<AuthRemoteDataSource>() }
    singleOf(::OfflineFirstAuthRepository) { bind<AuthRepository>() }
    // domain — use cases
    factoryOf(::LoginUseCase)
    // presentation — view models
    viewModelOf(::LoginViewModel)
}
```
```kotlin
// App.kt
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(coreModule, authModule, projectModule, tasksModule, profileModule)
        }
    }
}
```

- **Prefer `singleOf`/`factoryOf`/`viewModelOf`** (constructor references). Koin resolves each
  constructor parameter automatically — you can't forget a `get()` and the wiring can't silently
  drift when you add a constructor param.
- Fall back to the **lambda form** (`single { FirebaseAuth.getInstance() }`) only when you call a
  *factory method* rather than a constructor, or need a qualifier.
- Scopes: `singleOf` for repositories / `AppDatabase` / Firebase SDK instances (one instance for the
  app); `viewModelOf` for ViewModels; `factoryOf` for use cases and short-lived objects.
- Inject ViewModels **only at the Compose Root** via `koinViewModel()`. Never pass a ViewModel down
  the composable tree.

---

## 11. Checklist — Building a New Feature From Scratch

1. **Folders** — `feature_x/` with `domain/`, `data/`, `presentation/`, `di/`.
   - `domain/` → `model/`, `repository/`, `use_case/`
   - `data/` → `remote/` (+`dto/`), `local/`, `mapper/`, `repository/`
   - `presentation/` → one folder per screen
2. **Domain first (always).** Model (clean, no annotations) → repository/data-source *interface* →
   Use Cases (one per action) → feature `Error` enum if needed.
3. **Data second.** DTO (`@Serializable`) + Firestore data source → Entity (`@Entity`) + DAO →
   Mappers → repository/data-source impl (descriptive name, no `Impl`; caching logic here).
4. **Presentation third.** `State` (flat, unpack Result here) → `Action` (sealed) → `Event` (sealed,
   one-shot) → `ViewModel` (Actions→UseCases→State/Events) → `ScreenRoot`/`Screen` (Root wires
   ViewModel, Screen is stateless and previewable). Every `Screen` ships
   `@Preview(uiMode = UI_MODE_NIGHT_YES)` covering every state (loading/success/error/empty).
5. **Wire Koin.** `feature_x/di/FeatureXModule.kt` with `singleOf`/`factoryOf`/`viewModelOf`; add
   the module to `startKoin { modules(...) }` in `App.kt`.
6. **Check the rule.** Does `domain` import Android/Firebase or `data`? Does this feature import
   another feature? If yes → fix (move shared contracts to `core/`).

---

## 12. When to Graduate to Real Gradle Modules

You don't need to — this structure ships real apps as-is. Consider splitting into modules only when:
build times hurt, multiple teams edit in parallel and step on each other, or you want to enforce the
dependency rule with the *compiler* instead of review. Because packages already mirror
`:core:domain`, `:feature_x:data`, etc., the split is mostly moving folders and adding `build.gradle`
files — the boundaries are already there. Until then, single module + this discipline is the sweet
spot.

---

## Data Model (Firestore collections, mirrored as Room entities)

- **User** — `uid`, `displayName`, `email`, `photoUrl`
- **Project** — `id`, `name`, `ownerUid`, `createdAt`
- **InviteCode** — `code`, `projectId`, `expiresAt` (nullable), `isActive`
- **Role** (`projects/{projectId}/roles/{roleId}`) — `id`, `projectId`, `name`, `permissions` (booleans: `manageRoles`, `manageInviteCode`, `removeMembers`, `deleteProject`, `assignTasks`, `editAnyTask`, `manageTags`). A system `Leader` role is auto-created per project: all permissions `true`, immutable, assigned to the creator, cannot be edited/deleted/reassigned away by anyone else.
- **Membership** (`projects/{projectId}/members/{userId}`) — `projectId`, `userId`, `roleId`, and a **denormalized `permissions` snapshot** copied from the role at assignment time (refreshed whenever the member's role changes). Firestore security rules read this snapshot rather than chaining a lookup to the role document.
- **Task** (Phase 2+) — `id`, `projectId`, `title`, `description`, `assigneeIds`, `tagIds`, `status`, `dueDate`, `priority`, `createdAt`, `updatedAt`.

## Firestore Security Rules

- Only project members can read project subcollections.
- Writes to `roles`, `inviteCode`, member removal, and project deletion each require the corresponding boolean on the requesting user's own `membership.permissions` snapshot to be `true`.
- The Leader role's membership doc is never writable by any other member.

## Feature ↔ Package Mapping

| App concept | Package |
|---|---|
| Sign-in | `feature_auth` |
| Create/join project, roles, roster | `feature_project` |
| Task board (Phase 2+) | `feature_tasks` |
| Profile | `feature_profile` |

## Phase Roadmap

1. **Phase 1 — Auth + Projects + Roles.** Identity and permission foundation. See `specs/office/phase-1-auth-projects-roles.md`.
2. **Phase 2 — Task board.** Backlog/In Progress/Review/Done, tags, assignment.
3. **Phase 3 — Home screen stats.**
4. **Phase 4 — Polish.** Due dates, priority, comments, notifications.

Each phase gets its own spec in `specs/office/` before implementation starts (CLAUDE.md rule 3).

## Testing

| Concern | Tool |
|---|---|
| Unit tests | JUnit5, Turbine, AssertK, `kotlinx-coroutines-test` |
| UI tests | `ComposeTestRule` |
