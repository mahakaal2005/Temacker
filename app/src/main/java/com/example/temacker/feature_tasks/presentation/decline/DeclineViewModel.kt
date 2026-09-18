package com.example.temacker.feature_tasks.presentation.decline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.use_case.DeclineHandoffUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeclineViewModel(
    private val taskId: String,
    private val handoffId: String,
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val declineHandoff: DeclineHandoffUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DeclineState())
    val state = _state.asStateFlow()

    private val _events = Channel<DeclineEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: DeclineAction) {
        when (action) {
            is DeclineAction.OnReasonChange -> _state.update { it.copy(reason = action.value, error = null) }
            is DeclineAction.OnQuickChipClick -> _state.update { it.copy(reason = action.text, error = null) }
            DeclineAction.OnSendClick -> send()
            DeclineAction.OnBackClick -> viewModelScope.launch { _events.send(DeclineEvent.NavigateBack) }
            DeclineAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun send() = viewModelScope.launch {
        val reason = _state.value.reason.trim()
        if (reason.isBlank()) {
            _state.update { it.copy(error = UiText.DynamicString("Tell them why you're declining.")) }
            return@launch
        }

        val projectId = (observeCurrentProjectId().first() as? Result.Success)?.data
        if (projectId == null) {
            _state.update { it.copy(error = UiText.DynamicString("Couldn't find your project.")) }
            return@launch
        }

        _state.update { it.copy(isSending = true, error = null) }
        declineHandoff(projectId, taskId, handoffId, reason)
            .onSuccess { _events.send(DeclineEvent.NavigateToBoard) }
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
        _state.update { it.copy(isSending = false) }
    }
}
