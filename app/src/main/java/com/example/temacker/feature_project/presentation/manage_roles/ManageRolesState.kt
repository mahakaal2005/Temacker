package com.example.temacker.feature_project.presentation.manage_roles

import com.example.temacker.feature_project.domain.model.Role

data class ManageRolesState(
    val projectId: String? = null,
    val roles: List<Role> = emptyList(),
    val isLoading: Boolean = true,
    val isCreateDialogVisible: Boolean = false,
    val newRoleName: String = ""
)
