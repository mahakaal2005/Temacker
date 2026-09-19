package com.example.temacker.feature_tasks.presentation.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveInboxUseCase
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
class InboxViewModel(
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val observeInbox: ObserveInboxUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(InboxState())
    val state = _state.asStateFlow()

    private val _events = Channel<InboxEvent>()
    val events = _events.receiveAsFlow()

    init {
        val projectId = observeCurrentProjectId()
            .mapNotNull { result -> (result as? Result.Success)?.data }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.flatMapLatest { observeInbox(it) }.collect { result ->
                result
                    .onSuccess { inbox ->
                        val (waiting, earlier) = inbox.toRows(System.currentTimeMillis())
                        _state.update { it.copy(waiting = waiting, earlier = earlier, isLoading = false) }
                    }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
        }
    }

    fun onAction(action: InboxAction) {
        when (action) {
            is InboxAction.OnRowClick -> viewModelScope.launch {
                val row = action.row
                _events.send(
                    if (row.kind == InboxRowKind.OFFER_TO_YOU) InboxEvent.NavigateToIncoming(row.taskId, row.handoffId)
                    else InboxEvent.NavigateToTaskDetail(row.taskId)
                )
            }
            InboxAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }
}
