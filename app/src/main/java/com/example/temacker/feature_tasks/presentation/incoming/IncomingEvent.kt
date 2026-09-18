package com.example.temacker.feature_tasks.presentation.incoming

sealed interface IncomingEvent {
    data object NavigateBack : IncomingEvent
    data class NavigateToDecline(val taskId: String, val handoffId: String) : IncomingEvent
}
