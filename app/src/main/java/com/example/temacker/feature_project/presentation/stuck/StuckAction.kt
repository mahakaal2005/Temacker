package com.example.temacker.feature_project.presentation.stuck

sealed interface StuckAction {
    data object OnErrorDismissed : StuckAction
}
