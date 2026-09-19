package com.example.temacker.feature_tasks.presentation.queue

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.example.temacker.feature_tasks.domain.model.PendingWrite
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.PendingWriteType
import org.junit.jupiter.api.Test

class QueueMapperTest {

    private fun write(status: PendingWriteStatus, error: String? = null, createdAt: Long = 0) = PendingWrite(
        1, PendingWriteType.ACCEPT, "p", "t", "Sponsor deck", status, 0, error, createdAt, null
    )

    @Test
    fun `a failed write shows its reason in plain language`() {
        val row = write(PendingWriteStatus.FAILED, "CONFLICT").toRow(now = 10)
        assertThat(row.isFailed).isTrue()
        assertThat(row.detail).isEqualTo("It changed before this could send.")
        assertThat(row.title).isEqualTo("Accept handoff")
    }

    @Test
    fun `a waiting write shows how long ago it was saved`() {
        val row = write(PendingWriteStatus.PENDING, createdAt = 0).toRow(now = 2 * 60_000L)
        assertThat(row.isFailed).isFalse()
        assertThat(row.detail).isEqualTo("Saved 2 min ago")
    }

    @Test
    fun `age buckets read naturally and never go negative`() {
        assertThat(savedAgo(now = 5_000, createdAt = 0)).isEqualTo("Saved just now")
        assertThat(savedAgo(now = 3 * 3_600_000L, createdAt = 0)).isEqualTo("Saved 3 h ago")
        assertThat(savedAgo(now = 2 * 86_400_000L, createdAt = 0)).isEqualTo("Saved 2 d ago")
        assertThat(savedAgo(now = 0, createdAt = 9_000)).isEqualTo("Saved just now")
    }

    @Test
    fun `unknown error codes fall back to a generic reason`() {
        assertThat(failureText("SOMETHING_NEW")).isEqualTo("Couldn't be sent.")
        assertThat(failureText(null)).isEqualTo("Couldn't be sent.")
    }
}
