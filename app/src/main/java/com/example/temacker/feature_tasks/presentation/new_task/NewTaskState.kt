package com.example.temacker.feature_tasks.presentation.new_task

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText

@Stable
data class NewTaskState(
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null
)
