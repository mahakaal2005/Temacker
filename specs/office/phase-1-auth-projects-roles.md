# Phase 1 — Auth + Projects + Roles

Status: in progress

Foundation phase: identity, project membership, and the custom-role permission system. No tasks/board yet (Phase 2).

## Screens / Flows

- **Splash** — checks Firebase Auth session via `AuthRepository`. Routes to Login (signed out) or the project-selection state (signed in).
- **Login** — Google Sign-In button only.
- **No-project state** — shown when the signed-in user has no memberships. "Create project" or "Join with invite code".
- **Create project** — name input → creates `Project` doc, auto-creates the immutable `Leader` role, creates the creator's `Membership` with that role.
- **Join project** — invite code input → validates the code is `isActive` and not expired → creates a `Membership` with a default role (all permissions `false` — created automatically the first time a project has no other default role).
- **Home (stub)** — shows the current project's name and a switcher if the user belongs to more than one project. No stats (Phase 3).
- **Team roster** — lists members with their role badge. A member with `removeMembers` can remove another member (never the Leader). A member with `manageRoles` can reassign another member's role (never reassign the Leader's role away, never assign anyone else the Leader role).
- **Manage roles** — reachable from roster, gated on `manageRoles`. Lists roles, supports create/edit/delete with the 7 permission toggles. The `Leader` role is shown, locked, non-editable.
- **Profile** — display name, photo, sign out.

## Data Model

See `specs/ultimate_android_architecture.md` — Data Model section (User, Project, InviteCode, Role, Membership).

## Files (representative)

- `core/database/AppDatabase.kt`, `ProjectEntity.kt`, `RoleEntity.kt`, `MembershipEntity.kt`, `InviteCodeEntity.kt`, `ProjectDao.kt`, `RoleDao.kt`, `MembershipDao.kt`
- `feature/auth/domain/AuthRepository.kt`, `User.kt`
- `feature/auth/data/FirebaseAuthDataSource.kt`, `AuthRepositoryImpl` (or descriptively named)
- `feature/auth/presentation/SplashViewModel.kt`, `LoginScreen.kt`
- `feature/project/domain/{Project,Role,Membership,InviteCode}.kt`, `ProjectRepository.kt`, `RoleRepository.kt`
- `feature/project/data/*DataSource.kt`, `OfflineFirstProjectRepository.kt`, `OfflineFirstRoleRepository.kt`
- `feature/project/presentation/{CreateProject,JoinProject,Roster,ManageRoles}/*ViewModel.kt`, `*Screen.kt`, `*Root.kt`
- `di/AuthModule.kt`, `di/ProjectModule.kt`, `di/DatabaseModule.kt`
- `firestore.rules`

## Testing

ViewModel unit tests (JUnit5 + Turbine + AssertK + `UnconfinedTestDispatcher`) against fake repositories:

- Create project → Leader role auto-assigned, all permissions true.
- Join project → valid code succeeds; expired code rejected; inactive/unknown code rejected.
- Role permission toggles persist and reflect on the assigned members' denormalized snapshot.
- Member removal / role reassignment UI actions are hidden/blocked without the corresponding permission.
- Leader role cannot be edited, deleted, or reassigned away.

## Out of scope for Phase 1

Task board, tags, due dates, priority, comments, notifications, home stats.

## Completion

Mark this phase complete here, and log the session in `specs/logs/`, once implementation + tests + manual verification pass (CLAUDE.md rule 4).
