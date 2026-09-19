package com.example.temacker.feature_tasks.presentation.board

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.example.temacker.feature_tasks.domain.model.PendingWrite
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.PendingWriteType
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import org.junit.jupiter.api.Test

class BoardOverlayTest {

    private fun task(id: String) = Task(id, "p", "Task $id", null, "u", "Uma", TaskStatus.TODO, null, 0, "u", "Uma", 0, 0)

    private fun write(id: Long, taskId: String, status: PendingWriteStatus, draft: Task? = null) = PendingWrite(
        id, if (draft != null) PendingWriteType.CREATE_TASK else PendingWriteType.ACCEPT,
        "p", taskId, "Task $taskId", status, 0, null, id, draft
    )

    @Test
    fun `a queued create appears after the real tasks`() {
        val state = BoardState(tasks = listOf(task("a")), pendingWrites = listOf(write(1, "b", PendingWriteStatus.PENDING, task("b"))))
        assertThat(state.boardTasks().map { it.id }).containsExactly("a", "b")
    }

    @Test
    fun `a create that already reached Firestore is not shown twice`() {
        val state = BoardState(tasks = listOf(task("a"), task("b")), pendingWrites = listOf(write(1, "b", PendingWriteStatus.PENDING, task("b"))))
        assertThat(state.boardTasks().map { it.id }).containsExactly("a", "b")
    }

    @Test
    fun `a failed write outranks a queued one for the chip`() {
        val writes = listOf(write(1, "a", PendingWriteStatus.PENDING), write(2, "a", PendingWriteStatus.FAILED))
        assertThat(writes.syncStateOf("a")).isEqualTo(TaskSyncState.NOT_SENT)
        assertThat(listOf(write(1, "a", PendingWriteStatus.PENDING)).syncStateOf("a")).isEqualTo(TaskSyncState.QUEUED)
        assertThat(writes.syncStateOf("other")).isNull()
    }

    @Test
    fun `strip says nothing when online with an empty queue`() {
        assertThat(syncStrip(isOnline = true, queued = 0, failed = 0)).isNull()
    }

    @Test
    fun `strip prefers failures, then queue state, then offline`() {
        assertThat(syncStrip(false, queued = 2, failed = 1)!!.title).isEqualTo("1 couldn't be sent")
        assertThat(syncStrip(false, queued = 2, failed = 0)!!.title).isEqualTo("You're offline · 2 queued")
        assertThat(syncStrip(true, queued = 2, failed = 0)!!.title).isEqualTo("Sending 2 queued…")
        assertThat(syncStrip(false, queued = 0, failed = 0)!!.title).isEqualTo("You're offline")
    }
}
