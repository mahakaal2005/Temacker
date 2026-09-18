package com.example.temacker.feature_tasks.presentation.new_task

sealed interface NewTaskAction {
    data class OnTitleChange(val value: String) : NewTaskAction
    data class OnDescriptionChange(val value: String) : NewTaskAction
    data class OnDueDateSelected(val value: Long?) : NewTaskAction
    data object OnCreateClick : NewTaskAction
    data object OnBackClick : NewTaskAction
    data object OnErrorDismissed : NewTaskAction
}
