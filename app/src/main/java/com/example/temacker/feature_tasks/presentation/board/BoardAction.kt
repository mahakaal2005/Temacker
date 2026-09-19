package com.example.temacker.feature_tasks.presentation.board

import com.example.temacker.feature_tasks.domain.model.TaskStatus

sealed interface BoardAction {
    data class OnTabSelected(val status: TaskStatus) : BoardAction
    data object OnWaitingOnYouClick : BoardAction
    data class OnTaskClick(val taskId: String) : BoardAction
    data class OnIncomingTaskClick(val taskId: String, val handoffId: String) : BoardAction
    data object OnFabClick : BoardAction
    data object OnSyncStripClick : BoardAction
    data object OnErrorDismissed : BoardAction
    data class OnQueuedUndo(val writeId: Long) : BoardAction
    data object OnQueuedNoticeDismissed : BoardAction
}
