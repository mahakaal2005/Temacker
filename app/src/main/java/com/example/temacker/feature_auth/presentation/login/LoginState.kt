package com.example.temacker.feature_auth.presentation.login

import com.example.temacker.core.presentation.util.UiText

data class LoginState(
    val isLoading: Boolean = false,
    val error: UiText? = null
)
