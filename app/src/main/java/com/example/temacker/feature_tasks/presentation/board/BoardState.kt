package com.example.temacker.feature_tasks.presentation.board

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.PendingWrite
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus

// A write that was just queued; the screen offers Undo for it.
data class QueuedNotice(val writeId: Long)

@Stable
data class BoardState(
    val projectId: String? = null,
    val tasks: List<Task> = emptyList(),
    val pendingHandoffs: List<Handoff> = emptyList(),
    val pendingWrites: List<PendingWrite> = emptyList(),
    val queuedNotice: QueuedNotice? = null,
    val isOnline: Boolean = true,
    val selectedTab: TaskStatus = TaskStatus.TODO,
    val isFilteredToPending: Boolean = false,
    val canCreateTask: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null
)
