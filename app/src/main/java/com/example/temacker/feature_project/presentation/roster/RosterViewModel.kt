package com.example.temacker.feature_project.presentation.roster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.GenerateInviteCodeUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveActiveInviteCodeUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveRolesUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.domain.use_case.ReassignMemberRoleUseCase
import com.example.temacker.feature_project.domain.use_case.RemoveMemberUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RosterViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeMembers: ObserveMembersUseCase,
    private val observeRoles: ObserveRolesUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase,
    private val observeActiveInviteCode: ObserveActiveInviteCodeUseCase,
    private val generateInviteCode: GenerateInviteCodeUseCase,
    private val removeMember: RemoveMemberUseCase,
    private val reassignMemberRole: ReassignMemberRoleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RosterState())
    val state = _state.asStateFlow()

    private val _events = Channel<RosterEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeUserProjects().collectLatest { result ->
                result
                    .onSuccess { projects ->
                        val project = projects.firstOrNull() ?: return@onSuccess
                        _state.update { it.copy(projectId = project.id) }

                        launch {
                            observeMembers(project.id).collect { membersResult ->
                                membersResult
                                    .onSuccess { members -> _state.update { it.copy(members = members, isLoading = false) } }
                                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                            }
                        }
                        launch {
                            observeRoles(project.id).collect { rolesResult ->
                                rolesResult
                                    .onSuccess { roles -> _state.update { it.copy(roles = roles) } }
                                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                            }
                        }
                        launch {
                            observeCurrentMembership(project.id).collect { membershipResult ->
                                membershipResult
                                    .onSuccess { membership ->
                                        _state.update {
                                            it.copy(
                                                canManageInvite = membership?.permissions?.manageInviteCode == true,
                                                canRemoveMembers = membership?.permissions?.removeMembers == true,
                                                canManageRoles = membership?.permissions?.manageRoles == true
                                            )
                                        }
                                    }
                                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                            }
                        }
                        launch {
                            observeActiveInviteCode(project.id).collect { codeResult ->
                                codeResult
                                    .onSuccess { code -> _state.update { it.copy(inviteCode = code?.code) } }
                                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                            }
                        }
                    }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
    }

    fun onAction(action: RosterAction) {
        val projectId = _state.value.projectId ?: return
        when (action) {
            RosterAction.OnInviteClick -> {
                _state.update { it.copy(isInviteSheetVisible = true) }
                if (_state.value.inviteCode == null) generateCode(projectId)
            }
            RosterAction.OnDismissInviteSheet -> _state.update { it.copy(isInviteSheetVisible = false) }
            RosterAction.OnGenerateNewCodeClick -> generateCode(projectId)
            RosterAction.OnCopyCodeClick -> Unit // clipboard write happens in RosterRoot, which owns the Context.
            is RosterAction.OnMemberMoreClick -> _state.update { it.copy(menuForUserId = action.userId) }
            RosterAction.OnDismissMemberMenu -> _state.update { it.copy(menuForUserId = null) }
            is RosterAction.OnRemoveMemberClick -> {
                _state.update { it.copy(menuForUserId = null) }
                viewModelScope.launch {
                    removeMember(projectId, action.userId)
                        .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                }
            }
            is RosterAction.OnReassignRoleClick -> _state.update { it.copy(menuForUserId = null, reassignTargetUserId = action.userId) }
            RosterAction.OnDismissReassignSheet -> _state.update { it.copy(reassignTargetUserId = null) }
            is RosterAction.OnRoleSelected -> {
                val targetUserId = _state.value.reassignTargetUserId ?: return
                _state.update { it.copy(reassignTargetUserId = null) }
                viewModelScope.launch {
                    reassignMemberRole(projectId, targetUserId, action.roleId)
                        .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                }
            }
            RosterAction.OnManageRolesClick -> viewModelScope.launch { _events.send(RosterEvent.NavigateToManageRoles) }
            RosterAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun generateCode(projectId: String) = viewModelScope.launch {
        generateInviteCode(projectId)
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
    }
}
