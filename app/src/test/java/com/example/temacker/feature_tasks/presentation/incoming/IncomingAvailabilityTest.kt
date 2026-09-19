package com.example.temacker.feature_tasks.presentation.incoming

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import org.junit.jupiter.api.Test

class IncomingAvailabilityTest {

    private val task = Task("t1", "p1", "Deck", null, "u2", "Daniel", TaskStatus.TODO, null, 0, "u2", "Daniel", 0, 0)
    private fun handoff(status: HandoffStatus) =
        Handoff("h1", "t1", "u2", "Daniel", "u1", "Me", null, status, null, 0, null)
    private fun state(status: HandoffStatus?, task: Task? = this.task) = IncomingState(
        task = task, handoff = status?.let { handoff(it) }, isLoading = false, isTrailLoaded = true
    )

    @Test
    fun `an open offer has no notice`() {
        assertThat(state(HandoffStatus.OFFERED).unavailableNotice()).isNull()
    }

    @Test
    fun `nothing is shown while loading`() {
        assertThat(IncomingState().unavailableNotice()).isNull()
        assertThat(IncomingState(isLoading = false, isTrailLoaded = false).unavailableNotice()).isNull()
    }

    @Test
    fun `a missing task or handoff is unavailable`() {
        val title = "This handoff isn't available anymore"
        assertThat(state(HandoffStatus.OFFERED, task = null).unavailableNotice()?.title).isEqualTo(title)
        assertThat(state(null).unavailableNotice()?.title).isEqualTo(title)
    }

    @Test
    fun `an answered offer says what happened`() {
        assertThat(state(HandoffStatus.ACCEPTED).unavailableNotice()?.title).isEqualTo("You've already accepted this")
        assertThat(state(HandoffStatus.DECLINED).unavailableNotice()?.detail).isEqualTo("It stayed with Daniel.")
    }
}
