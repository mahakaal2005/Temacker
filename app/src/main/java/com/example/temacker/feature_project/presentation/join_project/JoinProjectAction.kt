package com.example.temacker.feature_project.presentation.join_project

sealed interface JoinProjectAction {
    data class OnCodeChange(val value: String) : JoinProjectAction
    data object OnJoinClick : JoinProjectAction
    data object OnBackClick : JoinProjectAction
}
