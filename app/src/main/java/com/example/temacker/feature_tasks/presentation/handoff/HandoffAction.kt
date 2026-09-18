package com.example.temacker.feature_tasks.presentation.handoff

sealed interface HandoffAction {
    data class OnMemberSelected(val uid: String) : HandoffAction
    data class OnNoteChange(val value: String) : HandoffAction
    data object OnSendClick : HandoffAction
    data object OnBackClick : HandoffAction
    data object OnErrorDismissed : HandoffAction
}
