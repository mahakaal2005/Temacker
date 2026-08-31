package com.example.temacker.feature_profile.presentation.profile

sealed interface ProfileEvent {
    data object NavigateToLogin : ProfileEvent
}
