package com.example.temacker.feature_tasks.data.notification

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.example.temacker.feature_tasks.domain.model.HandoffDestination
import org.junit.jupiter.api.Test

class HandoffPushTest {

    private fun payload(type: String, vararg extra: Pair<String, String>) = mapOf(
        "type" to type, "projectId" to "p1", "taskId" to "t1", "handoffId" to "h1", "taskTitle" to "Sponsor deck",
        "fromDisplayName" to "Daniel", "toDisplayName" to "Aisha", "note" to "", "declineReason" to ""
    ) + extra

    @Test
    fun `offer names the sender and task and carries actions`() {
        val push = parseHandoffPush(payload("HANDOFF_OFFERED", "note" to "Needs a final pass"))!!
        assertThat(push.title).isEqualTo("Daniel is handing you \"Sponsor deck\"")
        assertThat(push.text).isEqualTo("Needs a final pass")
        assertThat(push.hasActions).isTrue()
        assertThat(push.tapDestination).isEqualTo(HandoffDestination.INCOMING)
    }

    @Test
    fun `offer without a note falls back to a prompt`() {
        assertThat(parseHandoffPush(payload("HANDOFF_OFFERED"))!!.text).isEqualTo("Open it to accept or decline.")
    }

    @Test
    fun `accepted goes to task detail without actions`() {
        val push = parseHandoffPush(payload("HANDOFF_ACCEPTED"))!!
        assertThat(push.title).isEqualTo("Aisha accepted \"Sponsor deck\"")
        assertThat(push.hasActions).isFalse()
        assertThat(push.tapDestination).isEqualTo(HandoffDestination.TASK_DETAIL)
    }

    @Test
    fun `declined shows the reason, or says it is still yours`() {
        assertThat(parseHandoffPush(payload("HANDOFF_DECLINED", "declineReason" to "Away until the 30th"))!!.text)
            .isEqualTo("Away until the 30th")
        assertThat(parseHandoffPush(payload("HANDOFF_DECLINED"))!!.text).isEqualTo("It's still yours.")
    }

    @Test
    fun `offerer nudge uses the hours waiting`() {
        val push = parseHandoffPush(payload("HANDOFF_NUDGE_OFFERER", "hoursWaiting" to "19"))!!
        assertThat(push.title).isEqualTo("Still waiting — nobody has taken this yet")
        assertThat(push.text).isEqualTo("You offered \"Sponsor deck\" to Aisha 19 hours ago. It's still yours.")
        assertThat(push.hasActions).isFalse()
    }

    @Test
    fun `recipient nudge carries actions and opens the offer`() {
        val push = parseHandoffPush(payload("HANDOFF_NUDGE_RECIPIENT", "hoursWaiting" to "20"))!!
        assertThat(push.hasActions).isTrue()
        assertThat(push.tapDestination).isEqualTo(HandoffDestination.INCOMING)
    }

    @Test
    fun `unknown type or missing ids are dropped`() {
        assertThat(parseHandoffPush(payload("SOMETHING_ELSE"))).isNull()
        assertThat(parseHandoffPush(payload("HANDOFF_OFFERED", "handoffId" to ""))).isNull()
        assertThat(parseHandoffPush(payload("HANDOFF_OFFERED") - "taskId")).isNull()
        assertThat(parseHandoffPush(payload("HANDOFF_OFFERED"))).isNotNull()
    }

    @Test
    fun `blank names fall back to Someone`() {
        val push = parseHandoffPush(payload("HANDOFF_OFFERED", "fromDisplayName" to ""))!!
        assertThat(push.title).isEqualTo("Someone is handing you \"Sponsor deck\"")
    }
}
