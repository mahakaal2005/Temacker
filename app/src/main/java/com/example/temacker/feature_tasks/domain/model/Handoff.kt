package com.example.temacker.feature_tasks.domain.model

enum class HandoffStatus { OFFERED, ACCEPTED, DECLINED }

// OFFER is the ordinary hand-off; the two request kinds go to an approver and, when approved, move the task.
enum class HandoffKind { OFFER, DROP_REQUEST, TRANSFER_REQUEST }

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
    val respondedAt: Long?,
    val kind: HandoffKind = HandoffKind.OFFER,
    // Who a TRANSFER_REQUEST moves the task to once approved.
    val targetUid: String? = null,
    val targetDisplayName: String? = null
)
