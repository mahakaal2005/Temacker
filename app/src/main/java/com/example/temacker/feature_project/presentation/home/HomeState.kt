package com.example.temacker.feature_project.presentation.home

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText

@Stable
data class HomeState(
    val projectId: String? = null,
    val projectName: String = "",
    val memberCount: Int = 0,
    val isLeader: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null
)
