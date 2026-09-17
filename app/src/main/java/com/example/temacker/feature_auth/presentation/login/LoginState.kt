package com.example.temacker.feature_auth.presentation.login

import com.example.temacker.core.presentation.util.UiText

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null
)
