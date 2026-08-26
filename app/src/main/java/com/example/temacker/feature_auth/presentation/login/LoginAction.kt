package com.example.temacker.feature_auth.presentation.login

sealed interface LoginAction {
    data object OnGoogleSignInClick : LoginAction
}
