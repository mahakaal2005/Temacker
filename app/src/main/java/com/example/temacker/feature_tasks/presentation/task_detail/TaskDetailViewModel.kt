package com.example.temacker.feature_tasks.presentation.task_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.use_case.DeleteTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.MarkTaskDoneUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectMemberUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveHandoffTrailUseCase
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
class TaskDetailViewModel(
    private val taskId: String,
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val observeCurrentProjectMember: ObserveCurrentProjectMemberUseCase,
    private val observeTask: ObserveTaskUseCase,
    private val observeHandoffTrail: ObserveHandoffTrailUseCase,
    private val offerHandoff: OfferHandoffUseCase,
    private val markTaskDone: MarkTaskDoneUseCase,
    private val deleteTask: DeleteTaskUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TaskDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<TaskDetailEvent>()
    val events = _events.receiveAsFlow()

    private var currentUid: String? = null
    private var currentProjectId: String? = null

    init {
        val projectId = observeCurrentProjectId()
            .mapNotNull { result -> (result as? Result.Success)?.data }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.collect { id -> currentProjectId = id }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeCurrentProjectMember(it) }.collect { result ->
                result.onSuccess { member ->
                    currentUid = member?.uid
                    _state.update { it.copy(canDelete = member?.canEditAnyTask == true) }
                }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeTask(it, taskId) }.collect { result ->
                result
                    .onSuccess { task -> _state.update { it.copy(task = task, isLoading = false, isCurrentUserHolder = task?.holderUid == currentUid) } }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeHandoffTrail(it, taskId) }.collect { result ->
                result
                    .onSuccess { trail -> _state.update { it.copy(trail = trail) } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
    }

    fun onAction(action: TaskDetailAction) {
        when (action) {
            TaskDetailAction.OnHandOffClick -> viewModelScope.launch { _events.send(TaskDetailEvent.NavigateToHandoff(taskId)) }
            TaskDetailAction.OnMoreClick -> _state.update { it.copy(isMenuVisible = true) }
            TaskDetailAction.OnDismissMenu -> _state.update { it.copy(isMenuVisible = false) }
            TaskDetailAction.OnHandBackToPreviousClick -> handBackToPrevious()
            TaskDetailAction.OnMarkDoneClick -> {
                _state.update { it.copy(isMenuVisible = false) }
                viewModelScope.launch {
                    val projectId = currentProjectId ?: return@launch
                    val uid = currentUid ?: return@launch
                    val displayName = _state.value.task?.holderDisplayName ?: return@launch
                    markTaskDone(projectId, taskId, uid, displayName).onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                }
            }
            TaskDetailAction.OnDeleteClick -> _state.update { it.copy(isMenuVisible = false, isDeleteConfirmVisible = true) }
            TaskDetailAction.OnDismissDeleteConfirm -> _state.update { it.copy(isDeleteConfirmVisible = false) }
            TaskDetailAction.OnConfirmDeleteClick -> confirmDelete()
            TaskDetailAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun handBackToPrevious() = viewModelScope.launch {
        _state.update { it.copy(isMenuVisible = false) }
        val projectId = currentProjectId ?: return@launch
        val previous = _state.value.trail
            .filter { it.status == HandoffStatus.ACCEPTED }
            .maxByOrNull { it.respondedAt ?: 0L }
            ?: return@launch
        offerHandoff(projectId, taskId, previous.fromUid, previous.fromDisplayName, note = null)
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
    }

    private fun confirmDelete() = viewModelScope.launch {
        _state.update { it.copy(isDeleteConfirmVisible = false) }
        val projectId = currentProjectId ?: return@launch
        val uid = currentUid ?: return@launch
        val displayName = _state.value.task?.holderDisplayName ?: return@launch
        deleteTask(projectId, taskId, uid, displayName)
            .onSuccess { _events.send(TaskDetailEvent.NavigateBack) }
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
    }
}
