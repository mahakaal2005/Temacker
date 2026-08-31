package com.example.temacker.feature_project.presentation.gate

sealed interface ProjectGateEvent {
    data object NavigateToHome : ProjectGateEvent
    data object NavigateToNoProject : ProjectGateEvent
}
