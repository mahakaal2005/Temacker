package com.example.temacker.feature_project.presentation.invited_first_run

sealed interface InvitedFirstRunEvent {
    data object NavigateToBoard : InvitedFirstRunEvent
}
