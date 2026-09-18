package com.example.temacker.feature_project.presentation.stuck

import androidx.compose.runtime.Stable
import com.example.temacker.core.domain.model.StuckHandoff
import com.example.temacker.core.presentation.util.UiText

@Stable
data class StuckState(
    val isLoading: Boolean = true,
    val stuckHandoffs: List<StuckHandoff> = emptyList(),
    val error: UiText? = null
)
