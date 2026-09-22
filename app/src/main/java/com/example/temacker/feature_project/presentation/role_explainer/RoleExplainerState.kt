package com.example.temacker.feature_project.presentation.role_explainer

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.presentation.role_copy.Capability

@Stable
data class RoleExplainerState(
    val memberName: String = "",
    val roleName: String = "",
    val granted: List<Capability> = emptyList(),
    val setLine: String? = null,
    val showReadOnlyNote: Boolean = true,
    val isLoading: Boolean = true,
    val error: UiText? = null
)
