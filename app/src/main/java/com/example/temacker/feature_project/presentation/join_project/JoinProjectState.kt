package com.example.temacker.feature_project.presentation.join_project

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText

@Stable
data class JoinProjectState(
    val code: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null
)
