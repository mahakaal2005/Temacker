package com.example.temacker.feature_tasks.presentation.incoming

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.Task

@Stable
data class IncomingState(
    val task: Task? = null,
    val handoff: Handoff? = null,
    val isLoading: Boolean = true,
    val isTrailLoaded: Boolean = false,
    val isResponding: Boolean = false,
    val error: UiText? = null
)
