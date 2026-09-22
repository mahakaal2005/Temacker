package com.example.temacker.feature_project.presentation.switch_project

sealed interface SwitchProjectEvent {
    data object NavigateBack : SwitchProjectEvent
}
