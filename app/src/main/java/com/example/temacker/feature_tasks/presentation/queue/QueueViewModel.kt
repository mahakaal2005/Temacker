package com.example.temacker.feature_tasks.presentation.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.use_case.DiscardPendingWriteUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveConnectivityUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObservePendingWritesUseCase
import com.example.temacker.feature_tasks.domain.use_case.RetryPendingWriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class QueueViewModel(
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val observePendingWrites: ObservePendingWritesUseCase,
    private val observeConnectivity: ObserveConnectivityUseCase,
    private val retryPendingWrite: RetryPendingWriteUseCase,
    private val discardPendingWrite: DiscardPendingWriteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QueueState())
    val state = _state.asStateFlow()

    init {
        val projectId = observeCurrentProjectId()
            .mapNotNull { result -> (result as? Result.Success)?.data }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.flatMapLatest { observePendingWrites(it) }.collect { writes ->
                val rows = writes.map { it.toRow(System.currentTimeMillis()) }
                _state.update { it.copy(failed = rows.filter { r -> r.isFailed }, waiting = rows.filterNot { r -> r.isFailed }, isLoading = false) }
            }
        }
        viewModelScope.launch {
            observeConnectivity().collect { online -> _state.update { it.copy(isOnline = online) } }
        }
    }

    fun onAction(action: QueueAction) {
        when (action) {
            is QueueAction.OnRetryClick -> viewModelScope.launch { retryPendingWrite(action.id) }
            is QueueAction.OnDiscardClick -> viewModelScope.launch { discardPendingWrite(action.id) }
        }
    }
}
