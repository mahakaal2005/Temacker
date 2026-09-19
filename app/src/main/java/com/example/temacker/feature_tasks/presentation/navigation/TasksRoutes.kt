package com.example.temacker.feature_tasks.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable data object BoardRoute
@Serializable data object InboxRoute
@Serializable data object NewTaskRoute
@Serializable data object NotificationRationaleRoute
@Serializable data class TaskDetailRoute(val taskId: String)
@Serializable data class HandoffRoute(val taskId: String)
@Serializable data class IncomingRoute(val taskId: String, val handoffId: String)
@Serializable data class DeclineRoute(val taskId: String, val handoffId: String)
