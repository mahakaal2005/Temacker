package com.example.temacker.feature_project.presentation.manage_roles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.domain.use_case.CreateRoleUseCase
import com.example.temacker.feature_project.domain.use_case.DeleteRoleUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveRolesUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.domain.use_case.UpdateRoleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageRolesViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeRoles: ObserveRolesUseCase,
    private val createRole: CreateRoleUseCase,
    private val updateRole: UpdateRoleUseCase,
    private val deleteRole: DeleteRoleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ManageRolesState())
    val state = _state.asStateFlow()

    private val _events = Channel<ManageRolesEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeUserProjects().collectLatest { projects ->
                val project = projects.firstOrNull() ?: return@collectLatest
                _state.update { it.copy(projectId = project.id) }
                observeRoles(project.id).collect { roles -> _state.update { it.copy(roles = roles, isLoading = false) } }
            }
        }
    }

    fun onAction(action: ManageRolesAction) {
        val projectId = _state.value.projectId
        when (action) {
            ManageRolesAction.OnBackClick -> viewModelScope.launch { _events.send(ManageRolesEvent.NavigateBack) }
            ManageRolesAction.OnAddRoleClick -> _state.update { it.copy(isCreateDialogVisible = true) }
            ManageRolesAction.OnDismissCreateDialog -> _state.update { it.copy(isCreateDialogVisible = false, newRoleName = "") }
            is ManageRolesAction.OnNewRoleNameChange -> _state.update { it.copy(newRoleName = action.value) }
            ManageRolesAction.OnCreateRoleConfirm -> {
                val name = _state.value.newRoleName.trim()
                if (projectId != null && name.isNotBlank()) {
                    viewModelScope.launch {
                        createRole(projectId, name, RolePermissions.NONE)
                            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                    }
                }
                _state.update { it.copy(isCreateDialogVisible = false, newRoleName = "") }
            }
            is ManageRolesAction.OnPermissionToggle -> {
                val role = _state.value.roles.firstOrNull { it.id == action.roleId }
                if (projectId != null && role != null) {
                    viewModelScope.launch {
                        updateRole(projectId, role.id, role.name, action.permissions)
                            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                    }
                }
            }
            is ManageRolesAction.OnDeleteRoleClick -> {
                if (projectId != null) {
                    viewModelScope.launch {
                        deleteRole(projectId, action.roleId)
                            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                    }
                }
            }
            ManageRolesAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }
}
