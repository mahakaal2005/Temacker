package com.example.temacker.feature_tasks.presentation.task_detail

sealed interface TaskDetailAction {
    data object OnHandOffClick : TaskDetailAction
    data object OnMoreClick : TaskDetailAction
    data object OnDismissMenu : TaskDetailAction
    data object OnHandBackToPreviousClick : TaskDetailAction
    data object OnMarkDoneClick : TaskDetailAction
    data object OnDeleteClick : TaskDetailAction
    data object OnDismissDeleteConfirm : TaskDetailAction
    data object OnConfirmDeleteClick : TaskDetailAction
    data object OnErrorDismissed : TaskDetailAction
}
