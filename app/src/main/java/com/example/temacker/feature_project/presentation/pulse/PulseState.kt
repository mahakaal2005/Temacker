package com.example.temacker.feature_project.presentation.pulse

import androidx.compose.runtime.Stable
import com.example.temacker.core.domain.model.TeamEvent
import com.example.temacker.core.presentation.util.UiText

@Stable
data class PulseState(
    val isLoading: Boolean = true,
    val events: List<TeamEvent> = emptyList(),
    val error: UiText? = null
)
