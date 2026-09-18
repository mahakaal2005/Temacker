package com.example.temacker.feature_tasks.data.remote

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRemoteDataSource {
    fun observeTasks(projectId: String): Flow<List<Task>>
    fun observeTask(projectId: String, taskId: String): Flow<Task?>
    fun observeHandoffs(projectId: String, taskId: String): Flow<List<Handoff>>
    fun observePendingHandoffs(projectId: String, toUid: String): Flow<List<Handoff>>

    suspend fun createTask(
        projectId: String,
        title: String,
        description: String?,
        dueDate: Long?,
        holderUid: String,
        holderDisplayName: String,
        createdByDisplayName: String
    ): Result<Task, DataError>

    suspend fun offerHandoff(
        projectId: String,
        taskId: String,
        toUid: String,
        toDisplayName: String,
        note: String?
    ): Result<Handoff, DataError>

    suspend fun acceptHandoff(projectId: String, taskId: String, handoffId: String): Result<Task, DataError>
    suspend fun declineHandoff(projectId: String, taskId: String, handoffId: String, reason: String): Result<Handoff, DataError>
    suspend fun markTaskDone(projectId: String, taskId: String): Result<Task, DataError>
    suspend fun deleteTask(projectId: String, taskId: String, byUid: String, byDisplayName: String): EmptyResult<DataError>
}
