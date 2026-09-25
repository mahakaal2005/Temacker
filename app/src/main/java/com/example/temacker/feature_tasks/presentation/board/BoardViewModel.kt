package com.example.temacker.feature_tasks.presentation.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.use_case.DiscardPendingWriteUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveBoardUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveConnectivityUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveOtherProjectQueueCountUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObservePendingWritesUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectMemberUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectSummaryUseCase
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
    private val observeCurrentProjectSummary: ObserveCurrentProjectSummaryUseCase,
    private val observeBoard: ObserveBoardUseCase,
    private val observePendingHandoffs: ObservePendingHandoffsUseCase,
    private val observeCurrentProjectMember: ObserveCurrentProjectMemberUseCase,
    private val observePendingWrites: ObservePendingWritesUseCase,
    private val observeOtherProjectQueueCount: ObserveOtherProjectQueueCountUseCase,
    private val observeConnectivity: ObserveConnectivityUseCase,
    private val discardPendingWrite: DiscardPendingWriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BoardState())
    val state = _state.asStateFlow()

    // Ids of queued writes already seen, so only a write that appears later raises the Undo notice.
    private var seenWriteIds: Set<Long>? = null

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
            observeCurrentProjectSummary().collect { result ->
                result.onSuccess { summary -> _state.update { it.copy(projectSummary = summary) } }
            }
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
            observeConnectivity().collect { online -> _state.update { it.copy(isOnline = online) } }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observePendingWrites(it) }.collect { writes ->
                val seen = seenWriteIds
                val fresh = if (seen == null) null else writes.lastOrNull { it.id !in seen && it.status == PendingWriteStatus.PENDING }
                seenWriteIds = writes.mapTo(HashSet()) { it.id }
                _state.update { it.copy(pendingWrites = writes, queuedNotice = fresh?.let { w -> QueuedNotice(w.id) } ?: it.queuedNotice) }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeOtherProjectQueueCount(it) }.collect { count ->
                _state.update { it.copy(queuedInOtherProjects = count) }
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
            BoardAction.OnSyncStripClick -> viewModelScope.launch { _events.send(BoardEvent.NavigateToQueue) }
            BoardAction.OnFabClick -> viewModelScope.launch { _events.send(BoardEvent.NavigateToNewTask) }
            BoardAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
            is BoardAction.OnQueuedUndo -> viewModelScope.launch {
                discardPendingWrite(action.writeId)
                _state.update { it.copy(queuedNotice = null) }
            }
            BoardAction.OnQueuedNoticeDismissed -> _state.update { it.copy(queuedNotice = null) }
        }
    }
}
