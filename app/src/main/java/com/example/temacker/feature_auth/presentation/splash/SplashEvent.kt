package com.example.temacker.feature_auth.presentation.splash

sealed interface SplashEvent {
    data object NavigateToLogin : SplashEvent
    data object NavigateToApp : SplashEvent
}
