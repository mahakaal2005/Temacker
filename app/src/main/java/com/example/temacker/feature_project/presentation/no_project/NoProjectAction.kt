package com.example.temacker.feature_project.presentation.no_project

sealed interface NoProjectAction {
    data object OnCreateProjectClick : NoProjectAction
    data object OnJoinProjectClick : NoProjectAction
    data object OnProfileClick : NoProjectAction
}
