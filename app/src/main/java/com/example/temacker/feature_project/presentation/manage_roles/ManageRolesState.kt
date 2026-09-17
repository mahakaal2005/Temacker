package com.example.temacker.feature_project.presentation.manage_roles

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.domain.model.Role

@Stable
data class ManageRolesState(
    val projectId: String? = null,
    val roles: List<Role> = emptyList(),
    val isLoading: Boolean = true,
    val isCreateDialogVisible: Boolean = false,
    val newRoleName: String = "",
    val error: UiText? = null
)
