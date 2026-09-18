package com.example.temacker.feature_tasks.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeBoard(projectId: String): Flow<Result<List<Task>, DataError>>
    fun observeTask(projectId: String, taskId: String): Flow<Result<Task?, DataError>>
    fun observeHandoffTrail(projectId: String, taskId: String): Flow<Result<List<Handoff>, DataError>>
    // Handoffs offered to the currently signed-in user — uid resolved internally via SessionManager.
    fun observePendingHandoffs(projectId: String): Flow<Result<List<Handoff>, DataError>>

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
    ): EmptyResult<DataError>

    suspend fun acceptHandoff(projectId: String, taskId: String, handoffId: String): EmptyResult<DataError>
    suspend fun declineHandoff(projectId: String, taskId: String, handoffId: String, reason: String): EmptyResult<DataError>
    suspend fun markTaskDone(projectId: String, taskId: String, byUid: String, byDisplayName: String): EmptyResult<DataError>
    suspend fun deleteTask(projectId: String, taskId: String, byUid: String, byDisplayName: String): EmptyResult<DataError>
}
