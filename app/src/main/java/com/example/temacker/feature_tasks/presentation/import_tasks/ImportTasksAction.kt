package com.example.temacker.feature_tasks.presentation.import_tasks

sealed interface ImportTasksAction {
    data class OnFileRead(val text: String) : ImportTasksAction
    data object OnFileUnreadable : ImportTasksAction
    data object OnImportClick : ImportTasksAction
    data object OnChooseAnotherClick : ImportTasksAction
    data object OnBackClick : ImportTasksAction
    data object OnErrorDismissed : ImportTasksAction
}
