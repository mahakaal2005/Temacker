package com.example.temacker.feature_tasks.data.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.example.temacker.core.data.database.PendingWriteEntity
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.PendingWriteType
import org.junit.jupiter.api.Test

class PendingWriteMapperTest {

    private fun entity(payload: String, type: PendingWriteType = PendingWriteType.CREATE_TASK) = PendingWriteEntity(
        id = 7, type = type.name, projectId = "p", taskId = "t", taskTitle = "Ship it", payload = payload,
        status = PendingWriteStatus.PENDING.name, attempts = 0, lastError = null, createdAt = 123
    )

    @Test
    fun `every payload survives a round trip`() {
        listOf(
            PendingPayload.CreateTask("Ship it", null, 5L, "u", "Uma", "Uma"),
            PendingPayload.Offer("v", "Vik", "please"),
            PendingPayload.Accept("h1"),
            PendingPayload.Decline("h1", "too busy"),
            PendingPayload.MarkDone("u", "Uma")
        ).forEach { assertThat(decodePayload(it.encode())).isEqualTo(it) }
    }

    @Test
    fun `garbage payload decodes to null`() {
        assertThat(decodePayload("not json")).isNull()
    }

    @Test
    fun `a queued create carries a draft task for the board`() {
        val write = entity(PendingPayload.CreateTask("Ship it", "desc", null, "u", "Uma", "Uma").encode()).toDomain()
        assertThat(write).isNotNull()
        assertThat(write!!.draftTask!!.title).isEqualTo("Ship it")
        assertThat(write.draftTask!!.id).isEqualTo("t")
    }

    @Test
    fun `other writes have no draft task`() {
        val write = entity(PendingPayload.Accept("h1").encode(), PendingWriteType.ACCEPT).toDomain()
        assertThat(write!!.draftTask).isNull()
        assertThat(PendingPayload.Accept("h1")).isInstanceOf(PendingPayload::class)
    }
}
