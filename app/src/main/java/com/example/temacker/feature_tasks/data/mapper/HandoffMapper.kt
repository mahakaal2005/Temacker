package com.example.temacker.feature_tasks.data.mapper

import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toHandoff(taskId: String): Handoff? {
    val fromUid = getString("fromUid") ?: return null
    val fromDisplayName = getString("fromDisplayName") ?: return null
    val toUid = getString("toUid") ?: return null
    val toDisplayName = getString("toDisplayName") ?: return null
    val status = getString("status")?.let { runCatching { HandoffStatus.valueOf(it) }.getOrNull() } ?: HandoffStatus.OFFERED
    return Handoff(
        id = id,
        taskId = taskId,
        fromUid = fromUid,
        fromDisplayName = fromDisplayName,
        toUid = toUid,
        toDisplayName = toDisplayName,
        note = getString("note"),
        status = status,
        declineReason = getString("declineReason"),
        offeredAt = getLong("offeredAt") ?: 0L,
        respondedAt = getLong("respondedAt")
    )
}

// projectId is denormalized onto the Firestore doc (not part of the domain model, which is always
// used within an already-projectId-scoped context) so the collectionGroup query behind
// observePendingHandoffs can filter by it directly — same collection-group rule shape Phase 1 needed
// for inviteCodes (see specs/logs/2026-09-15-invite-code-and-collection-group-rule-fix.md).
fun Handoff.toFirestoreMap(projectId: String): Map<String, Any?> = mapOf(
    "projectId" to projectId,
    "fromUid" to fromUid,
    "fromDisplayName" to fromDisplayName,
    "toUid" to toUid,
    "toDisplayName" to toDisplayName,
    "note" to note,
    "status" to status.name,
    "declineReason" to declineReason,
    "offeredAt" to offeredAt,
    "respondedAt" to respondedAt
)
