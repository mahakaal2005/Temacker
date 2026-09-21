package com.example.temacker.feature_tasks.data.worker

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.temacker.core.data.database.HandoffDao
import com.example.temacker.core.data.database.PendingWriteDao
import com.example.temacker.core.data.database.PendingWriteEntity
import com.example.temacker.core.data.database.TaskDao
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.data.mapper.PendingPayload
import com.example.temacker.feature_tasks.data.mapper.encode
import com.example.temacker.feature_tasks.data.remote.TaskRemoteDataSource
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.MAX_REPLAY_ATTEMPTS
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PendingWriteReplayerTest {

    private val remote = mockk<TaskRemoteDataSource>()
    private val taskDao = mockk<TaskDao>(relaxed = true)
    private val handoffDao = mockk<HandoffDao>(relaxed = true)
    private val pendingDao = mockk<PendingWriteDao>(relaxed = true)
    private val replayer = PendingWriteReplayer(remote, taskDao, handoffDao, pendingDao)

    private val task = Task(
        id = "t", projectId = "p", title = "Ship it", description = null, holderUid = "u", holderDisplayName = "Uma",
        status = TaskStatus.DOING, dueDate = null, timesHandedOver = 1, createdByUid = "u", createdByDisplayName = "Uma",
        createdAt = 1, updatedAt = 2
    )

    private fun row(id: Long, payload: String = PendingPayload.Accept("h$id").encode(), attempts: Int = 0) = PendingWriteEntity(
        id = id, type = "ACCEPT", projectId = "p", taskId = "t", taskTitle = "Ship it", payload = payload,
        status = PendingWriteStatus.PENDING.name, attempts = attempts, lastError = null, createdAt = id
    )

    @Test
    fun `a replayed write is mirrored to Room and leaves the queue`() = runTest {
        coEvery { pendingDao.getPending() } returns listOf(row(1))
        coEvery { remote.acceptHandoff("p", "t", "h1") } returns Result.Success(task)

        assertThat(replayer.drain()).isEqualTo(DrainOutcome.DONE)
        coVerify { taskDao.upsertAll(any()) }
        coVerify { pendingDao.delete(1) }
    }

    @Test
    fun `offline stops the drain and leaves every row alone`() = runTest {
        coEvery { pendingDao.getPending() } returns listOf(row(1), row(2))
        coEvery { remote.acceptHandoff("p", "t", "h1") } returns Result.Error(DataError.Network.NO_INTERNET)

        assertThat(replayer.drain()).isEqualTo(DrainOutcome.RETRY_LATER)
        coVerify(exactly = 0) { pendingDao.delete(any()) }
        coVerify(exactly = 0) { pendingDao.markFailed(any(), any()) }
        coVerify(exactly = 0) { pendingDao.incrementAttempts(any()) }
        coVerify(exactly = 0) { remote.acceptHandoff("p", "t", "h2") }
    }

    @Test
    fun `a rejected write is marked failed with its reason and the drain moves on`() = runTest {
        coEvery { pendingDao.getPending() } returns listOf(row(1), row(2))
        coEvery { remote.acceptHandoff("p", "t", "h1") } returns Result.Error(DataError.Network.CONFLICT)
        coEvery { remote.getHandoffStatus("p", "t", "h1") } returns Result.Success(HandoffStatus.DECLINED)
        coEvery { remote.acceptHandoff("p", "t", "h2") } returns Result.Success(task)

        assertThat(replayer.drain()).isEqualTo(DrainOutcome.DONE)
        coVerify { pendingDao.markFailed(1, "CONFLICT") }
        coVerify(exactly = 0) { pendingDao.delete(1) }
        coVerify { pendingDao.delete(2) }
    }

    @Test
    fun `an accept that already went through is treated as done, not failed`() = runTest {
        coEvery { pendingDao.getPending() } returns listOf(row(1))
        coEvery { remote.acceptHandoff("p", "t", "h1") } returns Result.Error(DataError.Network.CONFLICT)
        coEvery { remote.getHandoffStatus("p", "t", "h1") } returns Result.Success(HandoffStatus.ACCEPTED)

        assertThat(replayer.drain()).isEqualTo(DrainOutcome.DONE)
        coVerify { pendingDao.delete(1) }
        coVerify(exactly = 0) { pendingDao.markFailed(any(), any()) }
    }

    @Test
    fun `a decline that already went through is treated as done`() = runTest {
        val decline = row(1, payload = PendingPayload.Decline("h1", "busy").encode()).copy(type = "DECLINE")
        coEvery { pendingDao.getPending() } returns listOf(decline)
        coEvery { remote.declineHandoff("p", "t", "h1", "busy") } returns Result.Error(DataError.Network.CONFLICT)
        coEvery { remote.getHandoffStatus("p", "t", "h1") } returns Result.Success(HandoffStatus.DECLINED)

        assertThat(replayer.drain()).isEqualTo(DrainOutcome.DONE)
        coVerify { pendingDao.delete(1) }
    }

    @Test
    fun `a conflict stays failed when the status lookup fails`() = runTest {
        coEvery { pendingDao.getPending() } returns listOf(row(1))
        coEvery { remote.acceptHandoff("p", "t", "h1") } returns Result.Error(DataError.Network.CONFLICT)
        coEvery { remote.getHandoffStatus("p", "t", "h1") } returns Result.Error(DataError.Network.NO_INTERNET)

        replayer.drain()
        coVerify { pendingDao.markFailed(1, "CONFLICT") }
    }

    @Test
    fun `a server error counts an attempt and retries later`() = runTest {
        coEvery { pendingDao.getPending() } returns listOf(row(1))
        coEvery { remote.acceptHandoff("p", "t", "h1") } returns Result.Error(DataError.Network.SERVER_ERROR)

        assertThat(replayer.drain()).isEqualTo(DrainOutcome.RETRY_LATER)
        coVerify { pendingDao.incrementAttempts(1) }
    }

    @Test
    fun `the last allowed attempt fails the write instead of retrying`() = runTest {
        coEvery { pendingDao.getPending() } returns listOf(row(1, attempts = MAX_REPLAY_ATTEMPTS - 1))
        coEvery { remote.acceptHandoff("p", "t", "h1") } returns Result.Error(DataError.Network.SERVER_ERROR)

        assertThat(replayer.drain()).isEqualTo(DrainOutcome.DONE)
        coVerify { pendingDao.markFailed(1, "SERVER_ERROR") }
    }

    @Test
    fun `an unreadable payload is failed, never dropped`() = runTest {
        coEvery { pendingDao.getPending() } returns listOf(row(1, payload = "garbage"))

        assertThat(replayer.drain()).isEqualTo(DrainOutcome.DONE)
        coVerify { pendingDao.markFailed(1, "SERIALIZATION") }
        coVerify(exactly = 0) { pendingDao.delete(any()) }
    }
}
