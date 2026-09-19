package com.example.temacker.feature_tasks.domain.model

// A handoff joined with its task's title — Handoff itself doesn't carry the title.
data class InboxEntry(
    val handoff: Handoff,
    val taskTitle: String
)

data class Inbox(
    val waitingOnYou: List<InboxEntry> = emptyList(),
    val earlier: List<InboxEntry> = emptyList()
) {
    companion object {
        const val UNANSWERED_AFTER_MILLIS = 18 * 60 * 60 * 1000L

        // Waiting = offered to me and open. Earlier = things I offered that have an outcome or have
        // gone unanswered for 18h; younger open offers stay out so the list never becomes noise.
        fun from(entries: List<InboxEntry>, uid: String, nowMillis: Long): Inbox = Inbox(
            waitingOnYou = entries
                .filter { it.handoff.toUid == uid && it.handoff.status == HandoffStatus.OFFERED }
                .sortedByDescending { it.handoff.offeredAt },
            earlier = entries
                .filter {
                    it.handoff.fromUid == uid && it.handoff.toUid != uid &&
                        (it.handoff.status != HandoffStatus.OFFERED ||
                            it.handoff.offeredAt <= nowMillis - UNANSWERED_AFTER_MILLIS)
                }
                .sortedByDescending { it.handoff.respondedAt ?: it.handoff.offeredAt }
        )
    }
}
