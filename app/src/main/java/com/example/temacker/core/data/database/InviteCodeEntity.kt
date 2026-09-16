package com.example.temacker.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "invite_codes", indices = [Index("projectId")])
data class InviteCodeEntity(
    @PrimaryKey val code: String,
    val projectId: String,
    val expiresAt: Long?,
    val isActive: Boolean
)
