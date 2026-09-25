package com.example.temacker.feature_profile.presentation.profile

sealed interface ProfileEvent {
    data object NavigateToLogin : ProfileEvent
    // Left the only project — the gate decides what comes next (No-project screen).
    data object NavigateToProjectGate : ProfileEvent
}
