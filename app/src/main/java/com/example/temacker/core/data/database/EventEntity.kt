package com.example.temacker.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "events", indices = [Index("projectId"), Index("projectId", "at")])
data class EventEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val type: String,
    val taskId: String,
    val taskTitle: String,
    val byUid: String,
    val byDisplayName: String,
    val at: Long
)
