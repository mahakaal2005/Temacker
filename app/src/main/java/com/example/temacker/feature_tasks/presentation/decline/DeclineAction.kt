package com.example.temacker.feature_tasks.presentation.decline

sealed interface DeclineAction {
    data class OnReasonChange(val value: String) : DeclineAction
    data class OnQuickChipClick(val text: String) : DeclineAction
    data object OnSendClick : DeclineAction
    data object OnBackClick : DeclineAction
    data object OnErrorDismissed : DeclineAction
}
