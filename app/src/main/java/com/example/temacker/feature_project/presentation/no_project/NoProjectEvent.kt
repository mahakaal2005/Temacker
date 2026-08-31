package com.example.temacker.feature_project.presentation.no_project

sealed interface NoProjectEvent {
    data object NavigateToCreateProject : NoProjectEvent
    data object NavigateToJoinProject : NoProjectEvent
    data object NavigateToProfile : NoProjectEvent
}
