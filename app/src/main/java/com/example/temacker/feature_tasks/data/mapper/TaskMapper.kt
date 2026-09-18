package com.example.temacker.feature_tasks.data.mapper

import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toTask(projectId: String): Task? {
    val title = getString("title") ?: return null
    val holderUid = getString("holderUid") ?: return null
    val holderDisplayName = getString("holderDisplayName") ?: return null
    val status = getString("status")?.let { runCatching { TaskStatus.valueOf(it) }.getOrNull() } ?: TaskStatus.TODO
    val createdByUid = getString("createdByUid") ?: return null
    val createdByDisplayName = getString("createdByDisplayName") ?: return null
    return Task(
        id = id,
        projectId = projectId,
        title = title,
        description = getString("description"),
        holderUid = holderUid,
        holderDisplayName = holderDisplayName,
        status = status,
        dueDate = getLong("dueDate"),
        timesHandedOver = (getLong("timesHandedOver") ?: 0L).toInt(),
        createdByUid = createdByUid,
        createdByDisplayName = createdByDisplayName,
        createdAt = getLong("createdAt") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L
    )
}

fun Task.toFirestoreMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "description" to description,
    "holderUid" to holderUid,
    "holderDisplayName" to holderDisplayName,
    "status" to status.name,
    "dueDate" to dueDate,
    "timesHandedOver" to timesHandedOver,
    "createdByUid" to createdByUid,
    "createdByDisplayName" to createdByDisplayName,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)
