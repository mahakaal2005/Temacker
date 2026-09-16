package com.example.temacker.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "roles", indices = [Index("projectId")])
data class RoleEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val name: String,
    val manageRoles: Boolean,
    val manageInviteCode: Boolean,
    val removeMembers: Boolean,
    val deleteProject: Boolean,
    val assignTasks: Boolean,
    val editAnyTask: Boolean,
    val manageTags: Boolean,
    val isLeader: Boolean
)
