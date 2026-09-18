package com.example.temacker.feature_tasks.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.use_case.ObserveBoardUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectMemberUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObservePendingHandoffsUseCase
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
class BoardViewModel(
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val observeBoard: ObserveBoardUseCase,
    private val observePendingHandoffs: ObservePendingHandoffsUseCase,
    private val observeCurrentProjectMember: ObserveCurrentProjectMemberUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BoardState())
    val state = _state.asStateFlow()

    private val _events = Channel<BoardEvent>()
    val events = _events.receiveAsFlow()

    init {
        // Same distinctUntilChanged-before-flatMapLatest shape RosterViewModel uses — restarts
        // nested Firestore listeners only when the project id actually changes.
        val projectId = observeCurrentProjectId()
            .mapNotNull { result -> (result as? Result.Success)?.data }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.collect { id -> _state.update { it.copy(projectId = id) } }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeBoard(it) }.collect { result ->
                result
                    .onSuccess { tasks -> _state.update { it.copy(tasks = tasks, isLoading = false) } }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observePendingHandoffs(it) }.collect { result ->
                result
                    .onSuccess { pending -> _state.update { it.copy(pendingHandoffs = pending) } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeCurrentProjectMember(it) }.collect { result ->
                result
                    .onSuccess { member -> _state.update { it.copy(canCreateTask = member?.canAssignTasks == true) } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
    }

    fun onAction(action: BoardAction) {
        when (action) {
            is BoardAction.OnTabSelected -> _state.update { it.copy(selectedTab = action.status, isFilteredToPending = false) }
            BoardAction.OnWaitingOnYouClick -> _state.update { it.copy(isFilteredToPending = !it.isFilteredToPending) }
            is BoardAction.OnTaskClick -> viewModelScope.launch { _events.send(BoardEvent.NavigateToTaskDetail(action.taskId)) }
            is BoardAction.OnIncomingTaskClick -> viewModelScope.launch { _events.send(BoardEvent.NavigateToIncoming(action.taskId, action.handoffId)) }
            BoardAction.OnFabClick -> viewModelScope.launch { _events.send(BoardEvent.NavigateToNewTask) }
            BoardAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }
}
