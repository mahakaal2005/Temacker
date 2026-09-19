package com.example.temacker.feature_tasks.presentation.navigation

import com.example.temacker.feature_tasks.domain.model.HandoffDestination

// The project rides along because the app only shows one project; a tap for another must not open a dead screen.
data class NotificationTarget(val projectId: String, val route: Any)

// Null when the intent carries no usable target; caller then just stays where it is.
fun notificationTarget(destination: HandoffDestination?, projectId: String?, taskId: String?, handoffId: String?): NotificationTarget? {
    if (projectId.isNullOrBlank()) return null
    return notificationRoute(destination, taskId, handoffId)?.let { NotificationTarget(projectId, it) }
}

fun notificationRoute(destination: HandoffDestination?, taskId: String?, handoffId: String?): Any? {
    if (destination == null || taskId.isNullOrBlank()) return null
    return when (destination) {
        HandoffDestination.INCOMING -> handoffId?.takeIf { it.isNotBlank() }?.let { IncomingRoute(taskId, it) }
        HandoffDestination.DECLINE -> handoffId?.takeIf { it.isNotBlank() }?.let { DeclineRoute(taskId, it) }
        HandoffDestination.TASK_DETAIL -> TaskDetailRoute(taskId)
    }
}
