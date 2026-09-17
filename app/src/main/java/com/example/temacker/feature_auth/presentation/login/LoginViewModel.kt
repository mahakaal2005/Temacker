package com.example.temacker.feature_auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.R
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_auth.domain.use_case.RegisterWithEmailUseCase
import com.example.temacker.feature_auth.domain.use_case.SignInWithEmailUseCase
import com.example.temacker.feature_auth.domain.use_case.SignInWithGoogleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signInWithEmail: SignInWithEmailUseCase,
    private val registerWithEmail: RegisterWithEmailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnGoogleSignInClick -> googleSignIn()
            is LoginAction.OnEmailChange -> _state.update { it.copy(email = action.value, error = null) }
            is LoginAction.OnPasswordChange -> _state.update { it.copy(password = action.value, error = null) }
            LoginAction.OnToggleMode -> _state.update { it.copy(isRegisterMode = !it.isRegisterMode, error = null) }
            LoginAction.OnTogglePasswordVisibility -> _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            LoginAction.OnEmailSubmit -> emailSubmit()
        }
    }

    private fun googleSignIn() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        signInWithGoogle()
            .onSuccess { _events.send(LoginEvent.NavigateToApp) }
            .onFailure { error ->
                // Picker dismiss isn't a real failure — don't surface an error for it.
                if (error != DataError.Network.CANCELLED) {
                    _state.update { it.copy(error = error.toUiText()) }
                }
            }
        _state.update { it.copy(isLoading = false) }
    }

    private fun emailSubmit() = viewModelScope.launch {
        val email = state.value.email.trim()
        val password = state.value.password

        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = UiText.StringResource(R.string.error_missing_credentials)) }
            return@launch
        }

        _state.update { it.copy(isLoading = true, error = null) }
        val result = if (state.value.isRegisterMode) {
            registerWithEmail(email, password)
        } else {
            signInWithEmail(email, password)
        }
        result
            .onSuccess { _events.send(LoginEvent.NavigateToApp) }
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
        _state.update { it.copy(isLoading = false) }
    }
}
