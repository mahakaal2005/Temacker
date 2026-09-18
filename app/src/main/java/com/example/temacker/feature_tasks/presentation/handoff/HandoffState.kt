package com.example.temacker.feature_tasks.presentation.handoff

import androidx.compose.runtime.Stable
import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_tasks.domain.model.Task

@Stable
data class HandoffState(
    val task: Task? = null,
    val members: List<ProjectMember> = emptyList(),
    val selectedUid: String? = null,
    val note: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: UiText? = null
)
