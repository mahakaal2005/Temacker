package com.example.temacker.feature_tasks.presentation.task_detail

sealed interface TaskDetailAction {
    data object OnHandOffClick : TaskDetailAction
    data object OnMoreClick : TaskDetailAction
    data object OnDismissMenu : TaskDetailAction
    data object OnHandBackToPreviousClick : TaskDetailAction
    data object OnMarkDoneClick : TaskDetailAction
    data object OnReassignClick : TaskDetailAction
    data object OnDismissReassign : TaskDetailAction
    data class OnReassignTo(val uid: String) : TaskDetailAction
    data object OnAskDropClick : TaskDetailAction
    data object OnAskTransferClick : TaskDetailAction
    data object OnDismissRequest : TaskDetailAction
    data object OnConfirmDropClick : TaskDetailAction
    data class OnTransferTargetSelected(val uid: String) : TaskDetailAction
    data object OnDeleteClick : TaskDetailAction
    data object OnDismissDeleteConfirm : TaskDetailAction
    data object OnConfirmDeleteClick : TaskDetailAction
    data object OnErrorDismissed : TaskDetailAction
}
