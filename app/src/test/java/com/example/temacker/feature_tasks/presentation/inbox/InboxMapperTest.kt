package com.example.temacker.feature_tasks.presentation.inbox

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.Inbox
import com.example.temacker.feature_tasks.domain.model.InboxEntry
import org.junit.jupiter.api.Test

class InboxMapperTest {

    private val minute = 60_000L
    private val hour = 60 * minute
    private val now = 1_000 * hour

    private fun handoff(status: HandoffStatus, offeredAt: Long, respondedAt: Long? = null, reason: String? = null) =
        Handoff("h", "t", "me", "Me", "her", "Aisha", "note", status, reason, offeredAt, respondedAt)

    @Test
    fun `relative time steps from now to minutes to hours to yesterday`() {
        assertThat(relativeTime(now, now - 10_000)).isEqualTo("now")
        assertThat(relativeTime(now, now - 5 * minute)).isEqualTo("5m")
        assertThat(relativeTime(now, now - 2 * hour)).isEqualTo("2h")
        assertThat(relativeTime(now, now - 30 * hour)).isEqualTo("Yesterday")
    }

    @Test
    fun `unanswered offer row reports whole hours waiting`() {
        val inbox = Inbox(earlier = listOf(InboxEntry(handoff(HandoffStatus.OFFERED, now - 19 * hour - 30 * minute), "Stage plan")))
        val row = inbox.toRows(now).second.single()
        assertThat(row.kind).isEqualTo(InboxRowKind.UNANSWERED)
        assertThat(row.title).isEqualTo("Aisha still hasn't answered Stage plan")
        assertThat(row.detail).isEqualTo("19h unanswered · still yours")
    }

    @Test
    fun `declined row carries the reason, and a blank reason is dropped`() {
        val withReason = Inbox(earlier = listOf(InboxEntry(handoff(HandoffStatus.DECLINED, now - 5 * hour, now - 2 * hour, "away"), "Venue")))
        assertThat(withReason.toRows(now).second.single().detail).isEqualTo("2h · \"away\"")
        val blank = Inbox(earlier = listOf(InboxEntry(handoff(HandoffStatus.DECLINED, now - 5 * hour, now - 2 * hour, " "), "Venue")))
        assertThat(blank.toRows(now).second.single().detail).isEqualTo("2h")
    }

    @Test
    fun `waiting row is an offer to me`() {
        val inbox = Inbox(waitingOnYou = listOf(InboxEntry(handoff(HandoffStatus.OFFERED, now - 2 * hour), "Sponsor deck")))
        val row = inbox.toRows(now).first.single()
        assertThat(row.kind).isEqualTo(InboxRowKind.OFFER_TO_YOU)
        assertThat(row.title).isEqualTo("Me is handing you Sponsor deck")
        assertThat(row.detail).isEqualTo("2h · offer open")
    }
}
