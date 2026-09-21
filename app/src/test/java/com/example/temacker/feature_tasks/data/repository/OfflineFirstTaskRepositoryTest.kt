package com.example.temacker.feature_tasks.data.repository

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.example.temacker.core.data.database.HandoffDao
import com.example.temacker.core.data.database.HandoffEntity
import com.example.temacker.core.data.database.InboxRow
import com.example.temacker.core.data.database.PendingWriteDao
import com.example.temacker.core.data.database.TaskDao
import com.example.temacker.core.domain.network.ConnectivityObserver
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.data.remote.TaskRemoteDataSource
import com.example.temacker.feature_tasks.data.worker.PendingWriteScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class OfflineFirstTaskRepositoryTest {

    private val remote = mockk<TaskRemoteDataSource>(relaxed = true)
    private val taskDao = mockk<TaskDao>(relaxed = true)
    private val handoffDao = mockk<HandoffDao>(relaxed = true)
    private val pendingDao = mockk<PendingWriteDao>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val connectivity = mockk<ConnectivityObserver>()
    private val scheduler = mockk<PendingWriteScheduler>(relaxed = true)
    private val repo = OfflineFirstTaskRepository(remote, taskDao, handoffDao, pendingDao, sessionManager, connectivity, scheduler)

    private fun handoff(id: String, taskId: String) = HandoffEntity(
        id = id, taskId = taskId, projectId = "p", fromUid = "them", fromDisplayName = "Them", toUid = "me", toDisplayName = "Me",
        note = null, status = "OFFERED", declineReason = null, offeredAt = 1, respondedAt = null
    )

    @Test
    fun `answering the same offer twice offline queues it once`() = runTest {
        every { connectivity.isOnline } returns flowOf(false)
        coEvery { taskDao.getTitle("t") } returns "Task"
        coEvery { pendingDao.countPendingFor("ACCEPT", "t") } returnsMany listOf(0, 1)
        coEvery { pendingDao.insert(any()) } returns 1L

        assertThat(repo.acceptHandoff("p", "t", "h")).isEqualTo(Result.Success(Unit))
        assertThat(repo.acceptHandoff("p", "t", "h")).isEqualTo(Result.Success(Unit))

        coVerify(exactly = 1) { pendingDao.insert(any()) }
    }

    @Test
    fun `an offer with a queued answer no longer counts as waiting`() = runTest {
        coEvery { sessionManager.getUid() } returns "me"
        every { remote.observePendingHandoffs("p", "me") } returns emptyFlow()
        every { handoffDao.observePendingForUser("p", "me") } returns flowOf(listOf(handoff("h1", "t1"), handoff("h2", "t2")))
        every { pendingDao.observeAnsweredTaskIds("p") } returns flowOf(listOf("t1"))

        val result = repo.observePendingHandoffs("p").first() as Result.Success

        assertThat(result.data.map { it.id }).containsExactly("h2")
    }

    @Test
    fun `the inbox drops offers to me that already have a queued answer`() = runTest {
        coEvery { sessionManager.getUid() } returns "me"
        every { remote.observePendingHandoffs("p", "me") } returns emptyFlow()
        every { remote.observeSentHandoffs("p", "me") } returns emptyFlow()
        every { remote.observeTasks("p") } returns emptyFlow()
        every { handoffDao.observeInbox("p", "me") } returns flowOf(
            listOf(InboxRow(handoff("h1", "t1"), "One"), InboxRow(handoff("h2", "t2"), "Two"))
        )
        every { pendingDao.observeAnsweredTaskIds("p") } returns flowOf(listOf("t1"))

        val result = repo.observeInbox("p").first() as Result.Success

        assertThat(result.data.map { it.taskTitle }).containsExactly("Two")
    }
}
