package com.example.temacker.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "handoffs", indices = [Index("taskId"), Index("projectId")])
data class HandoffEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val projectId: String,
    val fromUid: String,
    val fromDisplayName: String,
    val toUid: String,
    val toDisplayName: String,
    val note: String?,
    val status: String,
    val declineReason: String?,
    val offeredAt: Long,
    val respondedAt: Long?
)
