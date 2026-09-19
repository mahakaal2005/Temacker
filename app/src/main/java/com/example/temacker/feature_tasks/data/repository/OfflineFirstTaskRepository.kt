package com.example.temacker.feature_tasks.data.repository

import com.example.temacker.core.data.database.HandoffDao
import com.example.temacker.core.data.database.TaskDao
import com.example.temacker.core.data.firebase.toFirestoreDataError
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.asEmptyResult
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.feature_tasks.data.mapper.toDomain
import com.example.temacker.feature_tasks.data.mapper.toEntity
import com.example.temacker.feature_tasks.data.remote.TaskRemoteDataSource
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.InboxEntry
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OfflineFirstTaskRepository(
    private val remote: TaskRemoteDataSource,
    private val taskDao: TaskDao,
    private val handoffDao: HandoffDao,
    private val sessionManager: SessionManager
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
        handoffDao.observePendingForUser(projectId, uid).map { it.map { e -> e.toDomain() } }.collect { send(Result.Success(it)) }
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
            .map { rows -> rows.map { InboxEntry(it.handoff.toDomain(), it.taskTitle) } }
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
    ): Result<Task, DataError> = remote.createTask(projectId, title, description, dueDate, holderUid, holderDisplayName, createdByDisplayName)
        .onSuccess { taskDao.upsertAll(listOf(it.toEntity())) }

    override suspend fun offerHandoff(
        projectId: String,
        taskId: String,
        toUid: String,
        toDisplayName: String,
        note: String?
    ): EmptyResult<DataError> = remote.offerHandoff(projectId, taskId, toUid, toDisplayName, note)
        .onSuccess { handoffDao.upsertAll(listOf(it.toEntity(projectId))) }
        .asEmptyResult()

    override suspend fun acceptHandoff(projectId: String, taskId: String, handoffId: String): EmptyResult<DataError> =
        remote.acceptHandoff(projectId, taskId, handoffId)
            .onSuccess { taskDao.upsertAll(listOf(it.toEntity())) }
            .asEmptyResult()

    override suspend fun declineHandoff(projectId: String, taskId: String, handoffId: String, reason: String): EmptyResult<DataError> =
        remote.declineHandoff(projectId, taskId, handoffId, reason)
            .onSuccess { handoffDao.upsertAll(listOf(it.toEntity(projectId))) }
            .asEmptyResult()

    override suspend fun markTaskDone(projectId: String, taskId: String, byUid: String, byDisplayName: String): EmptyResult<DataError> =
        remote.markTaskDone(projectId, taskId, byUid, byDisplayName)
            .onSuccess { taskDao.upsertAll(listOf(it.toEntity())) }
            .asEmptyResult()

    override suspend fun deleteTask(projectId: String, taskId: String, byUid: String, byDisplayName: String): EmptyResult<DataError> =
        remote.deleteTask(projectId, taskId, byUid, byDisplayName)
            .onSuccess { taskDao.delete(taskId) }
}
