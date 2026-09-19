package com.example.temacker.feature_tasks.presentation.notification_rationale

sealed interface NotificationRationaleEvent {
    data object RequestPermission : NotificationRationaleEvent
    data object Close : NotificationRationaleEvent
}
