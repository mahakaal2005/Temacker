package com.example.temacker.feature_tasks.domain.model

// Where a notification tap (or its Decline action) should land; encoded into the launch intent.
enum class HandoffDestination { INCOMING, DECLINE, TASK_DETAIL }

const val EXTRA_DESTINATION = "temacker.notification.destination"
const val EXTRA_TASK_ID = "temacker.notification.taskId"
const val EXTRA_HANDOFF_ID = "temacker.notification.handoffId"
const val EXTRA_PROJECT_ID = "temacker.notification.projectId"
// Stamped per notification so an intent replayed after process death is not handled twice.
const val EXTRA_POSTED_AT = "temacker.notification.postedAt"
