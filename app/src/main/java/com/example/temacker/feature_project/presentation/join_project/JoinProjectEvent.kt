package com.example.temacker.feature_project.presentation.join_project

sealed interface JoinProjectEvent {
    data object NavigateBack : JoinProjectEvent
    data object NavigateToFirstRun : JoinProjectEvent
}
