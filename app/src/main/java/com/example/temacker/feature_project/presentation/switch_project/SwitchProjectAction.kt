package com.example.temacker.feature_project.presentation.switch_project

sealed interface SwitchProjectAction {
    data class OnProjectClick(val projectId: String) : SwitchProjectAction
}
