package com.example.temacker.feature_tasks.presentation.board

import com.example.temacker.feature_tasks.domain.model.PendingWrite
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.Task

enum class TaskSyncState { QUEUED, NOT_SENT }

data class SyncStrip(val title: String, val detail: String)

// Queued creates show on the Board before Firestore has them; a real doc with the same id wins.
fun BoardState.boardTasks(): List<Task> {
    val realIds = tasks.mapTo(HashSet()) { it.id }
    return tasks + pendingWrites.mapNotNull { it.draftTask }.filter { it.id !in realIds }
}

fun List<PendingWrite>.syncStateOf(taskId: String): TaskSyncState? {
    val forTask = filter { it.taskId == taskId }
    return when {
        forTask.any { it.status == PendingWriteStatus.FAILED } -> TaskSyncState.NOT_SENT
        forTask.isNotEmpty() -> TaskSyncState.QUEUED
        else -> null
    }
}

fun syncStrip(isOnline: Boolean, queued: Int, failed: Int): SyncStrip? = when {
    failed > 0 -> SyncStrip("$failed couldn't be sent", "Open the queue to retry or discard")
    queued > 0 && !isOnline -> SyncStrip("You're offline · $queued queued", "They send when you're back online")
    queued > 0 -> SyncStrip("Sending $queued queued…", "This clears by itself")
    !isOnline -> SyncStrip("You're offline", "Changes are saved on this phone and sent later")
    else -> null
}
