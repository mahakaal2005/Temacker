package com.example.temacker.feature_tasks.domain.model

enum class EventType { TASK_CREATED, TASK_DELETED, HANDOFF_OFFERED, HANDOFF_ACCEPTED, HANDOFF_DECLINED, TASK_MARKED_DONE }

data class Event(
    val id: String,
    val projectId: String,
    val type: EventType,
    val taskId: String,
    val taskTitle: String,
    val byUid: String,
    val byDisplayName: String,
    val at: Long
)
