package com.example.temacker.feature_tasks.data.worker

import com.example.temacker.core.data.database.HandoffDao
import com.example.temacker.core.data.database.PendingWriteDao
import com.example.temacker.core.data.database.PendingWriteEntity
import com.example.temacker.core.data.database.TaskDao
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.asEmptyResult
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.feature_tasks.data.mapper.PendingPayload
import com.example.temacker.feature_tasks.data.mapper.decodePayload
import com.example.temacker.feature_tasks.data.mapper.toEntity
import com.example.temacker.feature_tasks.data.remote.TaskRemoteDataSource
import com.example.temacker.feature_tasks.domain.model.ReplayVerdict
import com.example.temacker.feature_tasks.domain.model.replayVerdict

enum class DrainOutcome { DONE, RETRY_LATER }

// Replays queued writes oldest-first through the remote (not the repository, which would re-queue them).
class PendingWriteReplayer(
    private val remote: TaskRemoteDataSource,
    private val taskDao: TaskDao,
    private val handoffDao: HandoffDao,
    private val pendingDao: PendingWriteDao
) {
    suspend fun drain(): DrainOutcome {
        for (row in pendingDao.getPending()) {
            val payload = decodePayload(row.payload)
            if (payload == null) {
                pendingDao.markFailed(row.id, failureReason(DataError.Network.SERIALIZATION))
                continue
            }
            when (val result = replay(row, payload)) {
                is Result.Success -> pendingDao.delete(row.id)
                is Result.Error -> when (replayVerdict(result.error, row.attempts)) {
                    ReplayVerdict.WAIT -> return DrainOutcome.RETRY_LATER
                    ReplayVerdict.RETRY -> {
                        pendingDao.incrementAttempts(row.id)
                        return DrainOutcome.RETRY_LATER
                    }
                    ReplayVerdict.FAIL -> pendingDao.markFailed(row.id, failureReason(result.error))
                }
            }
        }
        return DrainOutcome.DONE
    }

    private suspend fun replay(row: PendingWriteEntity, payload: PendingPayload): EmptyResult<DataError> = when (payload) {
        is PendingPayload.CreateTask -> remote.createTask(
            row.projectId, payload.title, payload.description, payload.dueDate,
            payload.holderUid, payload.holderDisplayName, payload.createdByDisplayName, row.taskId
        ).onSuccess { taskDao.upsertAll(listOf(it.toEntity())) }.asEmptyResult()

        is PendingPayload.Offer -> remote.offerHandoff(row.projectId, row.taskId, payload.toUid, payload.toDisplayName, payload.note)
            .onSuccess { handoffDao.upsertAll(listOf(it.toEntity(row.projectId))) }.asEmptyResult()

        is PendingPayload.Accept -> remote.acceptHandoff(row.projectId, row.taskId, payload.handoffId)
            .onSuccess { taskDao.upsertAll(listOf(it.toEntity())) }.asEmptyResult()

        is PendingPayload.Decline -> remote.declineHandoff(row.projectId, row.taskId, payload.handoffId, payload.reason)
            .onSuccess { handoffDao.upsertAll(listOf(it.toEntity(row.projectId))) }.asEmptyResult()

        is PendingPayload.MarkDone -> remote.markTaskDone(row.projectId, row.taskId, payload.byUid, payload.byDisplayName)
            .onSuccess { taskDao.upsertAll(listOf(it.toEntity())) }.asEmptyResult()
    }
}

// Stored as the DataError name; the queue screen turns it into plain language.
fun failureReason(error: DataError): String = when (error) {
    is DataError.Network -> error.name
    is DataError.Local -> error.name
}
