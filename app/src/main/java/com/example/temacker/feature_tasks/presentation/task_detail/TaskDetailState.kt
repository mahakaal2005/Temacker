package com.example.temacker.feature_tasks.presentation.task_detail

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.Task

@Stable
data class TaskDetailState(
    val task: Task? = null,
    val trail: List<Handoff> = emptyList(),
    val isCurrentUserHolder: Boolean = false,
    val canDelete: Boolean = false,
    val isMenuVisible: Boolean = false,
    val isDeleteConfirmVisible: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null
)
