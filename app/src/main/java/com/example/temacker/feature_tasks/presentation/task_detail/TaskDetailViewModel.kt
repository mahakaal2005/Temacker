package com.example.temacker.feature_tasks.presentation.task_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import com.example.temacker.feature_tasks.domain.use_case.DeleteTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.MarkTaskDoneUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectMemberUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveHandoffTrailUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveProjectMembersUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.ReassignTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.RequestTaskChangeUseCase
import com.example.temacker.feature_tasks.domain.model.HandoffKind
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
    private val deleteTask: DeleteTaskUseCase,
    private val observeProjectMembers: ObserveProjectMembersUseCase,
    private val reassignTask: ReassignTaskUseCase,
    private val requestTaskChange: RequestTaskChangeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TaskDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<TaskDetailEvent>()
    val events = _events.receiveAsFlow()

    private var currentUid: String? = null
    private var currentProjectId: String? = null
    private var currentDisplayName: String = ""
    private var currentCanApprove = false

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
                    currentDisplayName = member?.displayName.orEmpty()
                    currentCanApprove = member?.isLeader == true || member?.canApproveTaskRequests == true
                    refreshRequestState()
                    _state.update { it.copy(canDelete = member?.canEditAnyTask == true, canReassign = member?.canAssignTasks == true) }
                }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeProjectMembers(it) }.collect { result ->
                result.onSuccess { members -> _state.update { it.copy(members = members) }; refreshRequestState() }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeTask(it, taskId) }.collect { result ->
                result
                    .onSuccess { task -> _state.update { it.copy(task = task, isLoading = false, isCurrentUserHolder = task?.holderUid == currentUid) }; refreshRequestState() }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
        }
        viewModelScope.launch {
            projectId.flatMapLatest { observeHandoffTrail(it, taskId) }.collect { result ->
                result
                    .onSuccess { trail -> _state.update { it.copy(trail = trail) }; refreshRequestState() }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
            }
        }
    }

    // Asking is for holders who can't approve; the ask goes to the Leader and is hidden while one is pending.
    private fun refreshRequestState() = _state.update { s ->
        val approver = s.members.firstOrNull { it.isLeader }
        val pending = s.trail.any { it.status == HandoffStatus.OFFERED && it.kind != HandoffKind.OFFER && it.fromUid == currentUid }
        s.copy(
            canRequestChange = s.isCurrentUserHolder && !currentCanApprove && approver != null && s.task?.status != TaskStatus.DONE && !pending,
            approverName = approver?.displayName,
            hasPendingRequest = pending
        )
    }

    private fun sendRequest(kind: HandoffKind, targetUid: String?) {
        _state.update { it.copy(requestStage = RequestStage.NONE, isMenuVisible = false) }
        viewModelScope.launch {
            val projectId = currentProjectId ?: return@launch
            val approver = _state.value.members.firstOrNull { it.isLeader } ?: return@launch
            val target = targetUid?.let { uid -> _state.value.members.firstOrNull { it.uid == uid } }
            requestTaskChange(projectId, taskId, kind, approver, target, note = null)
                .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
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
            TaskDetailAction.OnReassignClick -> _state.update { it.copy(isMenuVisible = false, isReassignVisible = true) }
            TaskDetailAction.OnDismissReassign -> _state.update { it.copy(isReassignVisible = false) }
            is TaskDetailAction.OnReassignTo -> {
                _state.update { it.copy(isReassignVisible = false) }
                viewModelScope.launch {
                    val projectId = currentProjectId ?: return@launch
                    val target = _state.value.members.firstOrNull { it.uid == action.uid } ?: return@launch
                    reassignTask(projectId, taskId, target.uid, target.displayName, currentDisplayName)
                        .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
                }
            }
            TaskDetailAction.OnAskDropClick -> _state.update { it.copy(isMenuVisible = false, requestStage = RequestStage.CONFIRM_DROP) }
            TaskDetailAction.OnAskTransferClick -> _state.update { it.copy(isMenuVisible = false, requestStage = RequestStage.PICK_TRANSFER_TARGET) }
            TaskDetailAction.OnDismissRequest -> _state.update { it.copy(requestStage = RequestStage.NONE) }
            TaskDetailAction.OnConfirmDropClick -> sendRequest(HandoffKind.DROP_REQUEST, null)
            is TaskDetailAction.OnTransferTargetSelected -> sendRequest(HandoffKind.TRANSFER_REQUEST, action.uid)
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
