package com.example.temacker.feature_project.presentation.home

sealed interface HomeEvent {
    data object NavigateToRoster : HomeEvent
}
