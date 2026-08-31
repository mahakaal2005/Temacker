package com.example.temacker.feature_auth.presentation.login

sealed interface LoginAction {
    data object OnGoogleSignInClick : LoginAction
    data class OnEmailChange(val value: String) : LoginAction
    data class OnPasswordChange(val value: String) : LoginAction
    data object OnToggleMode : LoginAction
    data object OnEmailSubmit : LoginAction
}
