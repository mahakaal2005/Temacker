package com.example.temacker.feature_project.presentation.join_project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.R
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import com.example.temacker.feature_project.domain.use_case.JoinProjectUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinProjectViewModel(
    private val joinProject: JoinProjectUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JoinProjectState())
    val state = _state.asStateFlow()

    private val _events = Channel<JoinProjectEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: JoinProjectAction) {
        when (action) {
            is JoinProjectAction.OnCodeChange -> _state.update { it.copy(code = action.value.uppercase(), error = null) }
            JoinProjectAction.OnJoinClick -> join()
            JoinProjectAction.OnBackClick -> viewModelScope.launch { _events.send(JoinProjectEvent.NavigateBack) }
        }
    }

    private fun join() = viewModelScope.launch {
        val code = _state.value.code.trim()
        if (code.isBlank()) {
            _state.update { it.copy(error = UiText.DynamicString("Enter an invite code.")) }
            return@launch
        }

        _state.update { it.copy(isLoading = true, error = null) }
        val user = authRepository.observeUser().first()
        if (user == null) {
            _state.update { it.copy(isLoading = false, error = UiText.DynamicString("You're signed out.")) }
            return@launch
        }

        joinProject(code, user.displayName, user.photoUrl)
            .onSuccess { _events.send(JoinProjectEvent.NavigateToHome) }
            .onFailure { error ->
                // The join transaction reports a bad/expired code as CONFLICT; everywhere else CONFLICT is generic.
                val text = if (error == DataError.Network.CONFLICT) UiText.StringResource(R.string.error_invite_code_invalid) else error.toUiText()
                _state.update { it.copy(error = text) }
            }
        _state.update { it.copy(isLoading = false) }
    }
}
