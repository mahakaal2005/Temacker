package com.example.temacker.feature_project.presentation.create_project

sealed interface CreateProjectAction {
    data class OnNameChange(val value: String) : CreateProjectAction
    data object OnCreateClick : CreateProjectAction
    data object OnBackClick : CreateProjectAction
}
