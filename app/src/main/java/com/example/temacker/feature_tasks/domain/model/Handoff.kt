package com.example.temacker.feature_tasks.domain.model

enum class HandoffStatus { OFFERED, ACCEPTED, DECLINED }

// The baton trail entry. fromDisplayName/toDisplayName are denormalized, same pattern as Task.
data class Handoff(
    val id: String,
    val taskId: String,
    val fromUid: String,
    val fromDisplayName: String,
    val toUid: String,
    val toDisplayName: String,
    val note: String?,
    val status: HandoffStatus,
    val declineReason: String?,
    val offeredAt: Long,
    val respondedAt: Long?
)
