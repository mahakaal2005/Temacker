package com.example.temacker.feature_project.presentation.switch_project

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText

data class SwitchProjectRow(
    val projectId: String,
    val name: String,
    val memberCount: Int,
    val roleName: String,
    val isSelected: Boolean
)

@Stable
data class SwitchProjectState(
    val rows: List<SwitchProjectRow> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null
)
