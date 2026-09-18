package com.example.temacker.core.domain.model

enum class TeamEventType { TASK_CREATED, TASK_DELETED, HANDOFF_OFFERED, HANDOFF_ACCEPTED, HANDOFF_DECLINED, TASK_MARKED_DONE }

data class TeamEvent(
    val id: String,
    val type: TeamEventType,
    val taskTitle: String,
    val byDisplayName: String,
    val at: Long
)
