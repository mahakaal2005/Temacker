package com.example.temacker.feature_tasks.data.mapper

import com.example.temacker.feature_tasks.domain.model.Event
import com.example.temacker.feature_tasks.domain.model.EventType
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toEvent(projectId: String): Event? {
    val type = getString("type")?.let { runCatching { EventType.valueOf(it) }.getOrNull() } ?: return null
    val taskId = getString("taskId") ?: return null
    val taskTitle = getString("taskTitle") ?: return null
    val byUid = getString("byUid") ?: return null
    val byDisplayName = getString("byDisplayName") ?: return null
    return Event(
        id = id,
        projectId = projectId,
        type = type,
        taskId = taskId,
        taskTitle = taskTitle,
        byUid = byUid,
        byDisplayName = byDisplayName,
        at = getLong("at") ?: 0L
    )
}

fun eventFirestoreMap(type: EventType, taskId: String, taskTitle: String, byUid: String, byDisplayName: String): Map<String, Any?> = mapOf(
    "type" to type.name,
    "taskId" to taskId,
    "taskTitle" to taskTitle,
    "byUid" to byUid,
    "byDisplayName" to byDisplayName,
    "at" to System.currentTimeMillis()
)
