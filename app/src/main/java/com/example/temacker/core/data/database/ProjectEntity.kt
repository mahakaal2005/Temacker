package com.example.temacker.core.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ownerUid: String,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0") val isArchived: Boolean = false,
    val predecessorProjectId: String? = null
)
