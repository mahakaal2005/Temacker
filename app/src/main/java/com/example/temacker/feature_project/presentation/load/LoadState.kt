package com.example.temacker.feature_project.presentation.load

import androidx.compose.runtime.Stable
import com.example.temacker.core.domain.model.HolderLoad
import com.example.temacker.core.presentation.util.UiText

@Stable
data class LoadState(
    val isLoading: Boolean = true,
    val holderLoads: List<HolderLoad> = emptyList(),
    val error: UiText? = null
)
