package com.example.temacker.feature_tasks.presentation.task_detail

sealed interface TaskDetailEvent {
    data object NavigateBack : TaskDetailEvent
    data class NavigateToHandoff(val taskId: String) : TaskDetailEvent
}
