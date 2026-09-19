package com.example.temacker.feature_tasks.presentation.queue

sealed interface QueueAction {
    data class OnRetryClick(val id: Long) : QueueAction
    data class OnDiscardClick(val id: Long) : QueueAction
}
