package com.example.temacker.feature_project.presentation.invited_first_run

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText

@Stable
data class InvitedFirstRunState(
    val projectName: String = "",
    val roleName: String = "",
    val addedByLine: String? = null,
    val hasNoPermissions: Boolean = true,
    val canDo: List<String> = emptyList(),
    val needsRole: List<String> = emptyList(),
    val leaderName: String? = null,
    val isLoading: Boolean = true,
    val error: UiText? = null
)
