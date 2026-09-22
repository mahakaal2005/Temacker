package com.example.temacker.feature_project.presentation.invited_first_run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.presentation.role_copy.ALWAYS_ALLOWED
import com.example.temacker.feature_project.presentation.role_copy.addedByLine
import com.example.temacker.feature_project.presentation.role_copy.grantedCapabilities
import com.example.temacker.feature_project.presentation.role_copy.missingCapabilities
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InvitedFirstRunViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase,
    private val observeMembers: ObserveMembersUseCase,
    private val currentProjectProvider: CurrentProjectProvider
) : ViewModel() {

    private val _state = MutableStateFlow(InvitedFirstRunState())
    val state = _state.asStateFlow()

    private val _events = Channel<InvitedFirstRunEvent>()
    val events = _events.receiveAsFlow()

    init {
        val projectId = currentProjectProvider.observeCurrentProjectId()
            .mapNotNull { (it as? Result.Success)?.data }
            .distinctUntilChanged()
        val project = projectId.combine(observeUserProjects()) { id, projectsResult ->
            (projectsResult as? Result.Success)?.data?.firstOrNull { it.id == id }
        }.distinctUntilChanged()

        viewModelScope.launch {
            project.collect { p -> if (p != null) _state.update { it.copy(projectName = p.name) } }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeCurrentMembership(it) }.collect { result ->
                result
                    .onSuccess { membership ->
                        if (membership != null) {
                            _state.update {
                                it.copy(
                                    roleName = membership.roleName,
                                    addedByLine = addedByLine(membership),
                                    hasNoPermissions = membership.permissions.grantedCapabilities().isEmpty(),
                                    canDo = ALWAYS_ALLOWED + membership.permissions.grantedCapabilities().map { c -> c.label },
                                    needsRole = membership.permissions.missingCapabilities().map { c -> c.label },
                                    isLoading = false
                                )
                            }
                        }
                    }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeMembers(it) }.collect { result ->
                result.onSuccess { members ->
                    _state.update { it.copy(leaderName = members.firstOrNull { m -> m.isLeader }?.displayName) }
                }
            }
        }
    }

    fun onAction(action: InvitedFirstRunAction) {
        when (action) {
            InvitedFirstRunAction.OnGoToBoardClick -> viewModelScope.launch { _events.send(InvitedFirstRunEvent.NavigateToBoard) }
        }
    }
}
