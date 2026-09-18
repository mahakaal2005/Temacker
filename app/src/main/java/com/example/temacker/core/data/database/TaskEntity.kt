package com.example.temacker.core.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks", indices = [Index("projectId")])
data class TaskEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val description: String?,
    val holderUid: String,
    val holderDisplayName: String,
    val status: String,
    val dueDate: Long?,
    val timesHandedOver: Int,
    val createdByUid: String,
    val createdByDisplayName: String,
    val createdAt: Long,
    val updatedAt: Long
)
