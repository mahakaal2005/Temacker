package com.example.temacker.feature_project.presentation.succession

sealed interface SuccessionEvent {
    data object NavigateBack : SuccessionEvent
    data object NavigateToTeam : SuccessionEvent
}
