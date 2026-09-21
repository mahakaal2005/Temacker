package com.example.temacker.feature_tasks.data.repository

import com.example.temacker.core.data.database.HandoffDao
import com.example.temacker.core.data.database.PendingWriteDao
import com.example.temacker.core.data.database.PendingWriteEntity
import com.example.temacker.core.data.database.TaskDao
import com.example.temacker.core.data.firebase.toFirestoreDataError
import com.example.temacker.core.domain.network.ConnectivityObserver
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.asEmptyResult
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.feature_tasks.data.mapper.PendingPayload
import com.example.temacker.feature_tasks.data.mapper.encode
import com.example.temacker.feature_tasks.data.mapper.toDomain
import com.example.temacker.feature_tasks.data.mapper.toEntity
import com.example.temacker.feature_tasks.data.remote.TaskRemoteDataSource
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.InboxEntry
import com.example.temacker.feature_tasks.data.worker.PendingWriteScheduler
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import com.example.temacker.feature_tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class OfflineFirstTaskRepository(
    private val remote: TaskRemoteDataSource,
    private val taskDao: TaskDao,
    private val handoffDao: HandoffDao,
    private val pendingDao: PendingWriteDao,
    private val sessionManager: SessionManager,
    private val connectivity: ConnectivityObserver,
    private val scheduler: PendingWriteScheduler
) : TaskRepository {

    override fun observeBoard(projectId: String): Flow<Result<List<Task>, DataError>> = channelFlow {
        launch {
            remote.observeTasks(projectId)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { tasks ->
                    taskDao.upsertAll(tasks.map { it.toEntity() })
                    taskDao.deleteMissing(projectId, tasks.map { it.id })
                }
        }
        taskDao.observeByProject(projectId).map { it.map { e -> e.toDomain() } }.collect { send(Result.Success(it)) }
    }

    override fun observeTask(projectId: String, taskId: String): Flow<Result<Task?, DataError>> = channelFlow {
        launch {
            remote.observeTask(projectId, taskId)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { task ->
                    if (task != null) {
                        taskDao.upsertAll(listOf(task.toEntity()))
                    } else {
                        taskDao.delete(taskId)
                    }
                }
        }
        taskDao.observeOne(taskId).map { it?.toDomain() }.collect { send(Result.Success(it)) }
    }

    override fun observeHandoffTrail(projectId: String, taskId: String): Flow<Result<List<Handoff>, DataError>> = channelFlow {
        launch {
            remote.observeHandoffs(projectId, taskId)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { handoffs ->
                    handoffDao.upsertAll(handoffs.map { it.toEntity(projectId) })
                    handoffDao.deleteMissingForTask(taskId, handoffs.map { it.id })
                }
        }
        handoffDao.observeByTask(taskId).map { it.map { e -> e.toDomain() } }.collect { send(Result.Success(it)) }
    }

    override fun observePendingHandoffs(projectId: String): Flow<Result<List<Handoff>, DataError>> = channelFlow {
        val uid = sessionManager.getUid()
        if (uid == null) {
            send(Result.Success(emptyList()))
            return@channelFlow
        }
        launch {
            remote.observePendingHandoffs(projectId, uid)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { handoffs ->
                    handoffDao.upsertAll(handoffs.map { it.toEntity(projectId) })
                    handoffDao.deleteMissingPendingForUser(projectId, uid, handoffs.map { it.id })
                }
        }
        handoffDao.observePendingForUser(projectId, uid)
            .combine(pendingDao.observeAnsweredTaskIds(projectId)) { entities, answered ->
                entities.filter { it.taskId !in answered }.map { it.toDomain() }
            }
            .collect { send(Result.Success(it)) }
    }

    override fun observeInbox(projectId: String): Flow<Result<List<InboxEntry>, DataError>> = channelFlow {
        val uid = sessionManager.getUid()
        if (uid == null) {
            send(Result.Success(emptyList()))
            return@channelFlow
        }
        launch {
            remote.observePendingHandoffs(projectId, uid)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { handoffs ->
                    handoffDao.upsertAll(handoffs.map { it.toEntity(projectId) })
                    handoffDao.deleteMissingPendingForUser(projectId, uid, handoffs.map { it.id })
                }
        }
        launch {
            remote.observeSentHandoffs(projectId, uid)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { handoffs ->
                    handoffDao.upsertAll(handoffs.map { it.toEntity(projectId) })
                    handoffDao.deleteMissingSentByUser(projectId, uid, handoffs.map { it.id })
                }
        }
        // The join needs task titles in Room even when the Board was never opened this session.
        launch {
            remote.observeTasks(projectId)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { tasks ->
                    taskDao.upsertAll(tasks.map { it.toEntity() })
                    taskDao.deleteMissing(projectId, tasks.map { it.id })
                }
        }
        handoffDao.observeInbox(projectId, uid)
            .combine(pendingDao.observeAnsweredTaskIds(projectId)) { rows, answered ->
                // An offer to me with a queued answer is no longer waiting on me.
                rows.filterNot { it.handoff.toUid == uid && it.handoff.taskId in answered }
                    .map { InboxEntry(it.handoff.toDomain(), it.taskTitle) }
            }
            .collect { send(Result.Success(it)) }
    }

    override suspend fun createTask(
        projectId: String,
        title: String,
        description: String?,
        dueDate: Long?,
        holderUid: String,
        holderDisplayName: String,
        createdByDisplayName: String
    ): Result<Task, DataError> {
        // Generated here so a queued create and its replay write the same document.
        val taskId = UUID.randomUUID().toString()
        val payload = PendingPayload.CreateTask(title, description, dueDate, holderUid, holderDisplayName, createdByDisplayName)
        if (!isOnline()) return queueCreate(projectId, taskId, title, payload)
        return when (val result = remote.createTask(projectId, title, description, dueDate, holderUid, holderDisplayName, createdByDisplayName, taskId)) {
            is Result.Success -> result.also { taskDao.upsertAll(listOf(it.data.toEntity())) }
            is Result.Error -> if (result.error == DataError.Network.NO_INTERNET) queueCreate(projectId, taskId, title, payload) else result
        }
    }

    override suspend fun offerHandoff(
        projectId: String,
        taskId: String,
        toUid: String,
        toDisplayName: String,
        note: String?
    ): EmptyResult<DataError> = writeOrQueue(projectId, taskId, PendingPayload.Offer(toUid, toDisplayName, note)) {
        remote.offerHandoff(projectId, taskId, toUid, toDisplayName, note)
            .onSuccess { handoffDao.upsertAll(listOf(it.toEntity(projectId))) }
            .asEmptyResult()
    }

    override suspend fun acceptHandoff(projectId: String, taskId: String, handoffId: String): EmptyResult<DataError> =
        writeOrQueue(projectId, taskId, PendingPayload.Accept(handoffId)) {
            remote.acceptHandoff(projectId, taskId, handoffId)
                .onSuccess { taskDao.upsertAll(listOf(it.toEntity())) }
                .asEmptyResult()
        }

    override suspend fun declineHandoff(projectId: String, taskId: String, handoffId: String, reason: String): EmptyResult<DataError> =
        writeOrQueue(projectId, taskId, PendingPayload.Decline(handoffId, reason)) {
            remote.declineHandoff(projectId, taskId, handoffId, reason)
                .onSuccess { handoffDao.upsertAll(listOf(it.toEntity(projectId))) }
                .asEmptyResult()
        }

    override suspend fun markTaskDone(projectId: String, taskId: String, byUid: String, byDisplayName: String): EmptyResult<DataError> =
        writeOrQueue(projectId, taskId, PendingPayload.MarkDone(byUid, byDisplayName)) {
            remote.markTaskDone(projectId, taskId, byUid, byDisplayName)
                .onSuccess { taskDao.upsertAll(listOf(it.toEntity())) }
                .asEmptyResult()
        }

    // Delete stays online-only: it is destructive, so it never sits in a queue.
    override suspend fun deleteTask(projectId: String, taskId: String, byUid: String, byDisplayName: String): EmptyResult<DataError> =
        remote.deleteTask(projectId, taskId, byUid, byDisplayName)
            .onSuccess { taskDao.delete(taskId) }

    private suspend fun isOnline(): Boolean = connectivity.isOnline.first()

    // Batch writes can hang offline, so the connectivity check runs first; a transaction that still fails offline is queued too.
    private suspend fun writeOrQueue(
        projectId: String,
        taskId: String,
        payload: PendingPayload,
        call: suspend () -> EmptyResult<DataError>
    ): EmptyResult<DataError> {
        if (!isOnline()) return queue(projectId, taskId, taskDao.getTitle(taskId) ?: FALLBACK_TITLE, payload)
        val result = call()
        val offline = result is Result.Error && result.error == DataError.Network.NO_INTERNET
        return if (offline) queue(projectId, taskId, taskDao.getTitle(taskId) ?: FALLBACK_TITLE, payload) else result
    }

    private suspend fun queueCreate(projectId: String, taskId: String, title: String, payload: PendingPayload.CreateTask): Result<Task, DataError> {
        val createdAt = System.currentTimeMillis()
        queue(projectId, taskId, title, payload, createdAt)
        return Result.Success(
            Task(
                id = taskId, projectId = projectId, title = title, description = payload.description,
                holderUid = payload.holderUid, holderDisplayName = payload.holderDisplayName, status = TaskStatus.TODO,
                dueDate = payload.dueDate, timesHandedOver = 0, createdByUid = payload.holderUid,
                createdByDisplayName = payload.createdByDisplayName, createdAt = createdAt, updatedAt = createdAt
            )
        )
    }

    private suspend fun queue(
        projectId: String,
        taskId: String,
        taskTitle: String,
        payload: PendingPayload,
        createdAt: Long = System.currentTimeMillis()
    ): EmptyResult<DataError> {
        // The same answer queued twice would fail as a conflict on replay, so a duplicate is ignored.
        if (payload !is PendingPayload.CreateTask && pendingDao.countPendingFor(payload.type.name, taskId) > 0) return Result.Success(Unit)
        pendingDao.insert(
            PendingWriteEntity(
                type = payload.type.name, projectId = projectId, taskId = taskId, taskTitle = taskTitle,
                payload = payload.encode(), status = PendingWriteStatus.PENDING.name, attempts = 0,
                lastError = null, createdAt = createdAt
            )
        )
        scheduler.enqueue()
        return Result.Success(Unit)
    }

    private companion object {
        const val FALLBACK_TITLE = "a task"
    }
}
