package com.example.temacker.feature_tasks.domain.model

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import org.junit.jupiter.api.Test

class InboxTest {

    private val me = "me"
    private val now = 100 * Inbox.UNANSWERED_AFTER_MILLIS

    private fun entry(
        id: String,
        from: String,
        to: String,
        status: HandoffStatus,
        offeredAt: Long,
        respondedAt: Long? = null
    ) = InboxEntry(
        handoff = Handoff(
            id = id, taskId = "t-$id", fromUid = from, fromDisplayName = from, toUid = to, toDisplayName = to,
            note = null, status = status, declineReason = null, offeredAt = offeredAt, respondedAt = respondedAt
        ),
        taskTitle = "Task $id"
    )

    @Test
    fun `waiting holds only open offers to me, newest first`() {
        val inbox = Inbox.from(
            listOf(
                entry("old", "a", me, HandoffStatus.OFFERED, offeredAt = 10),
                entry("new", "b", me, HandoffStatus.OFFERED, offeredAt = 20),
                entry("answered", "a", me, HandoffStatus.ACCEPTED, offeredAt = 30, respondedAt = 40),
                entry("someone-elses", "a", "c", HandoffStatus.OFFERED, offeredAt = 50)
            ),
            me, now
        )
        assertThat(inbox.waitingOnYou.map { it.handoff.id }).containsExactly("new", "old")
    }

    @Test
    fun `earlier holds my accepted and declined offers, most recent outcome first`() {
        val inbox = Inbox.from(
            listOf(
                entry("acc", me, "a", HandoffStatus.ACCEPTED, offeredAt = 10, respondedAt = 100),
                entry("dec", me, "b", HandoffStatus.DECLINED, offeredAt = 20, respondedAt = 200)
            ),
            me, now
        )
        assertThat(inbox.earlier.map { it.handoff.id }).containsExactly("dec", "acc")
    }

    @Test
    fun `an open offer of mine appears in earlier only once it is 18h old`() {
        val cutoff = now - Inbox.UNANSWERED_AFTER_MILLIS
        val inbox = Inbox.from(
            listOf(
                entry("stale", me, "a", HandoffStatus.OFFERED, offeredAt = cutoff),
                entry("fresh", me, "b", HandoffStatus.OFFERED, offeredAt = cutoff + 1)
            ),
            me, now
        )
        assertThat(inbox.earlier.map { it.handoff.id }).containsExactly("stale")
    }

    @Test
    fun `no entries gives an empty inbox`() {
        val inbox = Inbox.from(emptyList(), me, now)
        assertThat(inbox.waitingOnYou).isEmpty()
        assertThat(inbox.earlier).isEmpty()
    }
}
