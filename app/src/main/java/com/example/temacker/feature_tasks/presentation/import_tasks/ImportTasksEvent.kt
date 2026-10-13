package com.example.temacker.feature_tasks.presentation.import_tasks

sealed interface ImportTasksEvent {
    data object NavigateBack : ImportTasksEvent
    data object NavigateToBoard : ImportTasksEvent
}
