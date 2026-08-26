package com.example.temacker.feature_auth.presentation.login

sealed interface LoginEvent {
    data object NavigateToApp : LoginEvent
}
