package com.example.temacker.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProjectEntity::class, RoleEntity::class, MembershipEntity::class, InviteCodeEntity::class, TaskEntity::class, HandoffEntity::class],
    version = 4,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3), AutoMigration(from = 3, to = 4)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun roleDao(): RoleDao
    abstract fun membershipDao(): MembershipDao
    abstract fun inviteCodeDao(): InviteCodeDao
    abstract fun taskDao(): TaskDao
    abstract fun handoffDao(): HandoffDao
}
