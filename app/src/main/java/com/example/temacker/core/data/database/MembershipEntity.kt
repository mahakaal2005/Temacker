package com.example.temacker.core.data.database

import androidx.room.Entity

@Entity(tableName = "memberships", primaryKeys = ["projectId", "userId"])
data class MembershipEntity(
    val projectId: String,
    val userId: String,
    val roleId: String,
    val roleName: String,
    val manageRoles: Boolean,
    val manageInviteCode: Boolean,
    val removeMembers: Boolean,
    val deleteProject: Boolean,
    val assignTasks: Boolean,
    val editAnyTask: Boolean,
    val manageTags: Boolean,
    val displayName: String,
    val photoUrl: String?,
    val joinedAt: Long
)
