package com.example.temacker.feature_project.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
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

// Picks the user's first project as "current" — a real switcher arrives once multi-project use
// is common; Phase 1 only needs one project to exist and stay visible.
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeMembers: ObserveMembersUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeUserProjects().collect { result ->
                result
                    .onSuccess { projects ->
                        val project = projects.firstOrNull()
                        if (project == null) {
                            _state.update { it.copy(isLoading = false) }
                            return@onSuccess
                        }
                        _state.update { it.copy(projectId = project.id, projectName = project.name, isLoading = false) }
                    }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
        }

        val projectId = observeUserProjects()
            .mapNotNull { result -> (result as? Result.Success)?.data?.firstOrNull()?.id }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.flatMapLatest { observeMembers(it) }.collect { membersResult ->
                membersResult
                    .onSuccess { members -> _state.update { it.copy(memberCount = members.size) } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeCurrentMembership(it) }.collect { membershipResult ->
                membershipResult
                    .onSuccess { membership -> _state.update { it.copy(isLeader = membership?.roleName == "Leader") } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnGoToRosterClick -> viewModelScope.launch { _events.send(HomeEvent.NavigateToRoster) }
            HomeAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }
}
