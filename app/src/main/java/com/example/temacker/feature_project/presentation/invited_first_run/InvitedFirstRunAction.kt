package com.example.temacker.feature_project.presentation.invited_first_run

sealed interface InvitedFirstRunAction {
    data object OnGoToBoardClick : InvitedFirstRunAction
}
