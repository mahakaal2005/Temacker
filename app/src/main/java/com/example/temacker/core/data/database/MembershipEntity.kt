package com.example.temacker.core.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "memberships", primaryKeys = ["projectId", "userId"], indices = [Index("projectId")])
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
    val joinedAt: Long,
    @ColumnInfo(defaultValue = "0") val isLeader: Boolean = false,
    val roleSetByUid: String? = null,
    val roleSetByDisplayName: String? = null,
    val roleSetAt: Long? = null
)
