package com.example.temacker.feature_tasks.presentation.handoff

sealed interface HandoffEvent {
    data object NavigateBack : HandoffEvent
}
