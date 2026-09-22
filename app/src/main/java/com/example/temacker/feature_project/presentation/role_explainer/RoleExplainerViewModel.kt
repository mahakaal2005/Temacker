package com.example.temacker.feature_project.presentation.role_explainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.presentation.role_copy.grantedCapabilities
import com.example.temacker.feature_project.presentation.role_copy.roleSetLine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RoleExplainerViewModel(
    private val userId: String,
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeMembers: ObserveMembersUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RoleExplainerState())
    val state = _state.asStateFlow()

    private val _events = Channel<RoleExplainerEvent>()
    val events = _events.receiveAsFlow()

    init {
        val projectId = observeUserProjects()
            .mapNotNull { result -> (result as? Result.Success)?.data?.firstOrNull()?.id }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.flatMapLatest { observeMembers(it) }.collect { result ->
                result
                    .onSuccess { members ->
                        val member = members.firstOrNull { it.userId == userId }
                        if (member == null) {
                            _state.update { it.copy(isLoading = false, error = UiText.DynamicString("This member is no longer in the project.")) }
                        } else {
                            _state.update {
                                it.copy(
                                    memberName = member.displayName,
                                    roleName = member.roleName,
                                    granted = member.permissions.grantedCapabilities(),
                                    setLine = roleSetLine(member),
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                    }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeCurrentMembership(it) }.collect { result ->
                result.onSuccess { me -> _state.update { it.copy(showReadOnlyNote = me?.permissions?.manageRoles != true) } }
            }
        }
    }

    fun onAction(action: RoleExplainerAction) {
        when (action) {
            RoleExplainerAction.OnBackClick -> viewModelScope.launch { _events.send(RoleExplainerEvent.NavigateBack) }
        }
    }
}
