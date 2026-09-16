package com.example.temacker.feature_profile.presentation.profile

sealed interface ProfileAction {
    data object OnSignOutClick : ProfileAction
    data object OnErrorDismissed : ProfileAction
}
