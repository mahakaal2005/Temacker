package com.example.temacker.feature_tasks.presentation.incoming

sealed interface IncomingAction {
    data object OnAcceptClick : IncomingAction
    data object OnDeclineClick : IncomingAction
    data object OnErrorDismissed : IncomingAction
}
