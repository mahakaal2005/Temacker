# 2026-09-17 (session 7) — Fix audit inconsistency #7: missing Room indices

## Context
Continuation of the one-by-one audit cleanup from `specs/logs/2026-09-15-manage-roles-reassign-fix-and-full-audit.md`.
Items #1–#6 were fixed in earlier sessions. This session tackled item #7, originally stated as:
"Missing `@Index("projectId")` on `RoleEntity`/`MembershipEntity`/`InviteCodeEntity` (all queried by
projectId in their DAOs), and no cascade-delete/bulk-delete-by-projectId on any of the three child
tables (deleting a project leaves orphaned rows)."

## Investigation
Checked whether the cascade-delete half of the premise holds: grepped for any project-deletion
feature and found none — `deleteProject` exists only as an unused `RolePermissions` boolean flag,
never wired to a use case, repository method, or UI action anywhere in the app. Orphaned child rows
literally cannot occur today. Presented this to the user with three scoping options; **user chose to
fix the indices now and leave bulk-delete-by-projectId as noted future work**, rather than add dead
DAO methods with nothing to call them.

Also found Room schema export wasn't configured (`room.schemaLocation` ksp arg missing from
`app/build.gradle.kts`), so `@AutoMigration` wasn't usable out of the box for the version bump this
fix requires. Given the app hasn't shipped, offered a `fallbackToDestructiveMigration()` shortcut vs.
setting up schema export properly — **user chose the proper setup**.

## Fix
- `app/build.gradle.kts`: added `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`.
- Built once at version 1 (no entity changes yet) to generate the baseline
  `app/schemas/.../1.json`, committed as-is.
- `RoleEntity.kt`/`MembershipEntity.kt`/`InviteCodeEntity.kt`: added `indices = [Index("projectId")]`
  to each `@Entity` annotation — matches the `projectId`-filtered queries already in
  `RoleDao`/`MembershipDao`/`InviteCodeDao`.
- `AppDatabase.kt`: bumped `version = 2`, added `autoMigrations = [AutoMigration(from = 1, to = 2)]`.
  Rebuilding generated `app/schemas/.../2.json`, validating the auto-migration against the two
  exported schemas.

## Build
`./gradlew assembleDebug lintDebug` both pass.

## Scope
Per CLAUDE.md rule 14, the cascade-delete/bulk-delete half of the item was explicitly left untouched
(dead code with no caller) and is called out above as future work, to be built alongside an actual
project-deletion feature when one exists.
