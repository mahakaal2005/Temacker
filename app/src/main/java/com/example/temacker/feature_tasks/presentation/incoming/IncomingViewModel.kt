package com.example.temacker.feature_tasks.presentation.incoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.use_case.AcceptHandoffUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveHandoffTrailUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveTaskUseCase
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
class IncomingViewModel(
    private val taskId: String,
    private val handoffId: String,
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val observeTask: ObserveTaskUseCase,
    private val observeHandoffTrail: ObserveHandoffTrailUseCase,
    private val acceptHandoff: AcceptHandoffUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(IncomingState())
    val state = _state.asStateFlow()

    private val _events = Channel<IncomingEvent>()
    val events = _events.receiveAsFlow()

    private var currentProjectId: String? = null

    init {
        val projectId = observeCurrentProjectId()
            .mapNotNull { result -> (result as? Result.Success)?.data }
            .distinctUntilChanged()

        viewModelScope.launch { projectId.collect { currentProjectId = it } }
        viewModelScope.launch {
            projectId.flatMapLatest { observeTask(it, taskId) }.collect { result ->
                result
                    .onSuccess { task -> _state.update { it.copy(task = task, isLoading = false) } }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeHandoffTrail(it, taskId) }.collect { result ->
                result
                    .onSuccess { trail -> _state.update { it.copy(handoff = trail.firstOrNull { h -> h.id == handoffId }) } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
    }

    fun onAction(action: IncomingAction) {
        when (action) {
            IncomingAction.OnAcceptClick -> accept()
            IncomingAction.OnDeclineClick -> viewModelScope.launch { _events.send(IncomingEvent.NavigateToDecline(taskId, handoffId)) }
            IncomingAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun accept() = viewModelScope.launch {
        val projectId = currentProjectId ?: return@launch
        _state.update { it.copy(isResponding = true, error = null) }
        acceptHandoff(projectId, taskId, handoffId)
            .onSuccess { _events.send(IncomingEvent.NavigateBack) }
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
        _state.update { it.copy(isResponding = false) }
    }
}
