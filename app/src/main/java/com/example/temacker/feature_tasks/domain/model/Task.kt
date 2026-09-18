package com.example.temacker.feature_tasks.domain.model

enum class TaskStatus { TODO, DOING, DONE }

// holderDisplayName is denormalized (same pattern as Membership.displayName) so the board never
// joins against feature_auth's User — see specs/ultimate_android_architecture.md Data Model.
data class Task(
    val id: String,
    val projectId: String,
    val title: String,
    val description: String?,
    val holderUid: String,
    val holderDisplayName: String,
    val status: TaskStatus,
    val dueDate: Long?,
    val timesHandedOver: Int,
    val createdByUid: String,
    val createdByDisplayName: String,
    val createdAt: Long,
    val updatedAt: Long
)
