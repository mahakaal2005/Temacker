package com.example.temacker.feature_tasks.presentation.inbox

sealed interface InboxEvent {
    data class NavigateToIncoming(val taskId: String, val handoffId: String) : InboxEvent
    data class NavigateToTaskDetail(val taskId: String) : InboxEvent
}
