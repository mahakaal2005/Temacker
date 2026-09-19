package com.example.temacker.feature_tasks.presentation.inbox

import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.Inbox
import com.example.temacker.feature_tasks.domain.model.InboxEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

fun Inbox.toRows(nowMillis: Long): Pair<List<InboxRowUi>, List<InboxRowUi>> =
    waitingOnYou.map { it.toWaitingRow(nowMillis) } to earlier.map { it.toEarlierRow(nowMillis) }

private fun InboxEntry.toWaitingRow(nowMillis: Long) = InboxRowUi(
    handoffId = handoff.id,
    taskId = handoff.taskId,
    title = "${handoff.fromDisplayName} is handing you $taskTitle",
    detail = "${relativeTime(nowMillis, handoff.offeredAt)} · offer open",
    kind = InboxRowKind.OFFER_TO_YOU
)

private fun InboxEntry.toEarlierRow(nowMillis: Long): InboxRowUi {
    val respondedAt = handoff.respondedAt ?: handoff.offeredAt
    return when (handoff.status) {
        HandoffStatus.OFFERED -> InboxRowUi(
            handoffId = handoff.id,
            taskId = handoff.taskId,
            title = "${handoff.toDisplayName} still hasn't answered $taskTitle",
            detail = "${(nowMillis - handoff.offeredAt) / HOUR}h unanswered · still yours",
            kind = InboxRowKind.UNANSWERED
        )
        HandoffStatus.ACCEPTED -> InboxRowUi(
            handoffId = handoff.id,
            taskId = handoff.taskId,
            title = "${handoff.toDisplayName} accepted $taskTitle",
            detail = relativeTime(nowMillis, respondedAt),
            kind = InboxRowKind.ACCEPTED
        )
        HandoffStatus.DECLINED -> InboxRowUi(
            handoffId = handoff.id,
            taskId = handoff.taskId,
            title = "${handoff.toDisplayName} declined $taskTitle",
            detail = listOfNotNull(
                relativeTime(nowMillis, respondedAt),
                handoff.declineReason?.takeIf { it.isNotBlank() }?.let { "\"$it\"" }
            ).joinToString(" · "),
            kind = InboxRowKind.DECLINED
        )
    }
}

// Same style as the mock: "now", "2h", "Yesterday", "Mon", then a date.
internal fun relativeTime(nowMillis: Long, thenMillis: Long): String {
    val age = (nowMillis - thenMillis).coerceAtLeast(0)
    return when {
        age < MINUTE -> "now"
        age < HOUR -> "${age / MINUTE}m"
        age < DAY -> "${age / HOUR}h"
        age < 2 * DAY -> "Yesterday"
        age < 7 * DAY -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(thenMillis))
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(thenMillis))
    }
}
