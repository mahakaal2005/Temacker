package com.example.temacker.feature_project.presentation.create_project

sealed interface CreateProjectEvent {
    data object NavigateBack : CreateProjectEvent
    data object NavigateToHome : CreateProjectEvent
}
