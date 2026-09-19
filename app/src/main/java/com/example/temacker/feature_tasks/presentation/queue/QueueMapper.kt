package com.example.temacker.feature_tasks.presentation.queue

import com.example.temacker.feature_tasks.domain.model.PendingWrite
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.PendingWriteType

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

fun PendingWriteType.label(): String = when (this) {
    PendingWriteType.CREATE_TASK -> "New task"
    PendingWriteType.OFFER -> "Hand off task"
    PendingWriteType.ACCEPT -> "Accept handoff"
    PendingWriteType.DECLINE -> "Decline handoff"
    PendingWriteType.MARK_DONE -> "Mark done"
}

// lastError holds the DataError name the replayer stored.
fun failureText(code: String?): String = when (code) {
    "CONFLICT", "NOT_FOUND" -> "It changed before this could send."
    "PERMISSION_DENIED", "FORBIDDEN", "UNAUTHORIZED" -> "You no longer have permission to do this."
    "SERVER_ERROR", "UNKNOWN" -> "The server kept failing."
    else -> "Couldn't be sent."
}

fun savedAgo(now: Long, createdAt: Long): String {
    val age = (now - createdAt).coerceAtLeast(0)
    return when {
        age < MINUTE -> "Saved just now"
        age < HOUR -> "Saved ${age / MINUTE} min ago"
        age < DAY -> "Saved ${age / HOUR} h ago"
        else -> "Saved ${age / DAY} d ago"
    }
}

fun PendingWrite.toRow(now: Long) = QueueRowUi(
    id = id,
    title = type.label(),
    taskTitle = taskTitle,
    detail = if (status == PendingWriteStatus.FAILED) failureText(lastError) else savedAgo(now, createdAt),
    isFailed = status == PendingWriteStatus.FAILED
)
