package com.example.temacker.feature_project.presentation.succession

sealed interface SuccessionAction {
    data class OnNameChange(val value: String) : SuccessionAction
    data object OnContinueClick : SuccessionAction
    data object OnBackToNameClick : SuccessionAction
    data object OnConfirmClick : SuccessionAction
    data object OnBackClick : SuccessionAction
}
