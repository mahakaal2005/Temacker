package com.example.temacker.feature_tasks.data.notification

import com.example.temacker.feature_tasks.domain.model.HandoffDestination

enum class HandoffPushType(val wire: String) {
    OFFERED("HANDOFF_OFFERED"),
    ACCEPTED("HANDOFF_ACCEPTED"),
    DECLINED("HANDOFF_DECLINED"),
    NUDGE_OFFERER("HANDOFF_NUDGE_OFFERER"),
    NUDGE_RECIPIENT("HANDOFF_NUDGE_RECIPIENT")
}

data class HandoffPush(
    val type: HandoffPushType,
    val projectId: String,
    val taskId: String,
    val handoffId: String,
    val title: String,
    val text: String,
    // Blank for pushes sent before the function started including it.
    val projectName: String = ""
) {
    // Only pushes that ask the reader to act carry Accept / Decline.
    val hasActions: Boolean get() = type == HandoffPushType.OFFERED || type == HandoffPushType.NUDGE_RECIPIENT
    val tapDestination: HandoffDestination
        get() = if (hasActions) HandoffDestination.INCOMING else HandoffDestination.TASK_DETAIL
}

// Public version shown on a locked screen when notification content is private.
const val PUBLIC_TITLE = "Temacker"
const val PUBLIC_TEXT = "A baton moved."

// Returns null for payloads we can't render (unknown type or missing ids), so nothing half-formed is shown.
fun parseHandoffPush(data: Map<String, String>): HandoffPush? {
    val type = HandoffPushType.entries.firstOrNull { it.wire == data["type"] } ?: return null
    val projectId = data["projectId"]?.takeIf { it.isNotBlank() } ?: return null
    val taskId = data["taskId"]?.takeIf { it.isNotBlank() } ?: return null
    val handoffId = data["handoffId"]?.takeIf { it.isNotBlank() } ?: return null
    val task = data["taskTitle"].orEmpty()
    val from = data["fromDisplayName"].orEmpty().ifBlank { "Someone" }
    val to = data["toDisplayName"].orEmpty().ifBlank { "Someone" }
    val note = data["note"].orEmpty()
    val reason = data["declineReason"].orEmpty()
    val hours = data["hoursWaiting"].orEmpty().ifBlank { "18" }

    val (title, text) = when (type) {
        HandoffPushType.OFFERED -> "$from is handing you \"$task\"" to note.ifBlank { "Open it to accept or decline." }
        HandoffPushType.ACCEPTED -> "$to accepted \"$task\"" to "It's theirs now."
        HandoffPushType.DECLINED -> "$to declined \"$task\"" to reason.ifBlank { "It's still yours." }
        HandoffPushType.NUDGE_OFFERER ->
            "Still waiting — nobody has taken this yet" to "You offered \"$task\" to $to $hours hours ago. It's still yours."
        HandoffPushType.NUDGE_RECIPIENT ->
            "A baton is waiting on you" to "$from offered you \"$task\" $hours hours ago. Accept or decline it."
    }
    return HandoffPush(type, projectId, taskId, handoffId, title, text, data["projectName"].orEmpty())
}
