package com.example.temacker.feature_project.presentation.succession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.domain.use_case.TriggerSuccessionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SuccessionViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase,
    private val triggerSuccession: TriggerSuccessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SuccessionState())
    val state = _state.asStateFlow()

    private val _events = Channel<SuccessionEvent>()
    val events = _events.receiveAsFlow()

    private var projectId: String? = null

    init {
        // Nav-graph-level gate: the only entry point is a Leader-only affordance on TeamScreen,
        // but this self-check bounces back immediately if reached any other way (deep link, back
        // stack manipulation) — the real enforcement is still Firestore rules + the use case guard.
        viewModelScope.launch {
            val id = (observeUserProjects().first() as? Result.Success)?.data?.firstOrNull()?.id
            if (id == null) {
                _events.send(SuccessionEvent.NavigateBack)
                return@launch
            }
            projectId = id
            val isLeader = (observeCurrentMembership(id).first() as? Result.Success)?.data?.isLeader == true
            if (!isLeader) _events.send(SuccessionEvent.NavigateBack)
        }
    }

    fun onAction(action: SuccessionAction) {
        when (action) {
            is SuccessionAction.OnNameChange -> _state.update { it.copy(newProjectName = action.value, error = null) }
            SuccessionAction.OnContinueClick -> {
                if (_state.value.newProjectName.isBlank()) {
                    _state.update { it.copy(error = UiText.DynamicString("Enter a name for the new cycle.")) }
                } else {
                    _state.update { it.copy(step = SuccessionStep.CONFIRM) }
                }
            }
            SuccessionAction.OnBackToNameClick -> _state.update { it.copy(step = SuccessionStep.NAME) }
            SuccessionAction.OnConfirmClick -> submit()
            SuccessionAction.OnBackClick -> viewModelScope.launch { _events.send(SuccessionEvent.NavigateBack) }
        }
    }

    private fun submit() = viewModelScope.launch {
        val id = projectId ?: return@launch
        _state.update { it.copy(isSubmitting = true, error = null) }
        triggerSuccession(id, _state.value.newProjectName.trim())
            .onSuccess { _events.send(SuccessionEvent.NavigateToTeam) }
            .onFailure { error -> _state.update { it.copy(isSubmitting = false, error = error.toUiText()) } }
    }
}
