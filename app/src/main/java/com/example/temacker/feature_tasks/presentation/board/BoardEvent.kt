package com.example.temacker.feature_tasks.presentation.board

sealed interface BoardEvent {
    data object NavigateToNewTask : BoardEvent
    data class NavigateToTaskDetail(val taskId: String) : BoardEvent
    data class NavigateToIncoming(val taskId: String, val handoffId: String) : BoardEvent
}
