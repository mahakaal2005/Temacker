package com.example.temacker.feature_tasks.presentation.queue

import androidx.compose.runtime.Stable

data class QueueRowUi(
    val id: Long,
    val title: String,
    val taskTitle: String,
    val detail: String,
    val isFailed: Boolean
)

@Stable
data class QueueState(
    val failed: List<QueueRowUi> = emptyList(),
    val waiting: List<QueueRowUi> = emptyList(),
    val isOnline: Boolean = true,
    val isLoading: Boolean = true
)
