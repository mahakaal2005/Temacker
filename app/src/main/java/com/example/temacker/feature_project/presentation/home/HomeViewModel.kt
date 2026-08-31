package com.example.temacker.feature_project.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Picks the user's first project as "current" — a real switcher arrives once multi-project use
// is common; Phase 1 only needs one project to exist and stay visible.
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
            observeUserProjects().collectLatest { projects ->
                val project = projects.firstOrNull()
                if (project == null) {
                    _state.update { it.copy(isLoading = false) }
                    return@collectLatest
                }
                _state.update { it.copy(projectId = project.id, projectName = project.name, isLoading = false) }

                launch {
                    observeMembers(project.id).collect { members ->
                        _state.update { it.copy(memberCount = members.size) }
                    }
                }
                launch {
                    observeCurrentMembership(project.id).collect { membership ->
                        _state.update { it.copy(isLeader = membership?.roleName == "Leader") }
                    }
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnGoToRosterClick -> viewModelScope.launch { _events.send(HomeEvent.NavigateToRoster) }
        }
    }
}
