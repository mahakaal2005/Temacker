package com.example.temacker.feature_auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_auth.domain.use_case.LoginUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnGoogleSignInClick -> login()
        }
    }

    private fun login() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        loginUseCase()
            .onSuccess { _events.send(LoginEvent.NavigateToApp) }
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
        _state.update { it.copy(isLoading = false) }
    }
}
