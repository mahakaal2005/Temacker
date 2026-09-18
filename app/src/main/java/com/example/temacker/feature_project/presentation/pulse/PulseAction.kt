package com.example.temacker.feature_project.presentation.pulse

sealed interface PulseAction {
    data object OnErrorDismissed : PulseAction
}
