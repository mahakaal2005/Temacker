package com.example.temacker.feature_tasks.presentation.new_task

sealed interface NewTaskEvent {
    data object NavigateBack : NewTaskEvent
    data object NavigateToBoard : NewTaskEvent
}
