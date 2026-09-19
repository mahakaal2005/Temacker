package com.example.temacker.feature_tasks.domain.model

enum class PendingWriteType { CREATE_TASK, OFFER, ACCEPT, DECLINE, MARK_DONE }

enum class PendingWriteStatus { PENDING, FAILED }

// A write saved on this phone while offline; draftTask is set only for CREATE_TASK so the Board can show it.
data class PendingWrite(
    val id: Long,
    val type: PendingWriteType,
    val projectId: String,
    val taskId: String,
    val taskTitle: String,
    val status: PendingWriteStatus,
    val attempts: Int,
    val lastError: String?,
    val createdAt: Long,
    val draftTask: Task?
)
