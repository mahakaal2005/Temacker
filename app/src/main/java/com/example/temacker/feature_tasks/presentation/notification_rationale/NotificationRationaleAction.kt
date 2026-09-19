package com.example.temacker.feature_tasks.presentation.notification_rationale

sealed interface NotificationRationaleAction {
    data object OnTurnOnClick : NotificationRationaleAction
    data object OnNotNowClick : NotificationRationaleAction
    data object OnPermissionResult : NotificationRationaleAction
}
