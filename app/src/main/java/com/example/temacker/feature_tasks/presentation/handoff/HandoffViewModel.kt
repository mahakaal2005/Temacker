package com.example.temacker.feature_tasks.presentation.handoff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveProjectMembersUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.OfferHandoffUseCase
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
class HandoffViewModel(
    private val taskId: String,
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val observeTask: ObserveTaskUseCase,
    private val observeProjectMembers: ObserveProjectMembersUseCase,
    private val offerHandoff: OfferHandoffUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HandoffState(isLoading = true))
    val state = _state.asStateFlow()

    private val _events = Channel<HandoffEvent>()
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
            projectId.flatMapLatest { observeProjectMembers(it) }.collect { result ->
                result
                    .onSuccess { members ->
                        val holderUid = _state.value.task?.holderUid
                        _state.update { it.copy(members = members.filter { m -> m.uid != holderUid }) }
                    }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
    }

    fun onAction(action: HandoffAction) {
        when (action) {
            is HandoffAction.OnMemberSelected -> _state.update { it.copy(selectedUid = action.uid) }
            is HandoffAction.OnNoteChange -> _state.update { it.copy(note = action.value) }
            HandoffAction.OnSendClick -> send()
            HandoffAction.OnBackClick -> viewModelScope.launch { _events.send(HandoffEvent.NavigateBack) }
            HandoffAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun send() = viewModelScope.launch {
        val projectId = currentProjectId ?: return@launch
        val target = _state.value.members.firstOrNull { it.uid == _state.value.selectedUid } ?: return@launch
        _state.update { it.copy(isSending = true, error = null) }
        offerHandoff(projectId, taskId, target.uid, target.displayName, _state.value.note.trim().ifBlank { null })
            .onSuccess { _events.send(HandoffEvent.NavigateBack) }
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
        _state.update { it.copy(isSending = false) }
    }
}
