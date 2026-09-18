package com.example.temacker.feature_tasks.presentation.decline

sealed interface DeclineEvent {
    data object NavigateBack : DeclineEvent
    data object NavigateToBoard : DeclineEvent
}
