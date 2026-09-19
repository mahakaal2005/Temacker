package com.example.temacker.feature_tasks.presentation.inbox

sealed interface InboxAction {
    data class OnRowClick(val row: InboxRowUi) : InboxAction
    data object OnErrorDismissed : InboxAction
}
