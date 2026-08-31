package com.example.temacker.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProjectEntity::class, RoleEntity::class, MembershipEntity::class, InviteCodeEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun roleDao(): RoleDao
    abstract fun membershipDao(): MembershipDao
    abstract fun inviteCodeDao(): InviteCodeDao
}
