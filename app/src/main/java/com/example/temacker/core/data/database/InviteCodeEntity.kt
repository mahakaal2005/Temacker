package com.example.temacker.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invite_codes")
data class InviteCodeEntity(
    @PrimaryKey val code: String,
    val projectId: String,
    val expiresAt: Long?,
    val isActive: Boolean
)
