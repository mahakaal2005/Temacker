package com.example.temacker.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "pending_writes", indices = [Index("projectId"), Index("taskId")])
data class PendingWriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val projectId: String,
    val taskId: String,
    val taskTitle: String,
    val payload: String,
    val status: String,
    val attempts: Int,
    val lastError: String?,
    val createdAt: Long
)
