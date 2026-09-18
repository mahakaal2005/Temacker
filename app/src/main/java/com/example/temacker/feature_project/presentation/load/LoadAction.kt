package com.example.temacker.feature_project.presentation.load

sealed interface LoadAction {
    data object OnErrorDismissed : LoadAction
}
