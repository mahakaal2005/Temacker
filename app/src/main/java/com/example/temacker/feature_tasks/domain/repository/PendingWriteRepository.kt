package com.example.temacker.feature_tasks.domain.repository

import com.example.temacker.feature_tasks.domain.model.PendingWrite
import kotlinx.coroutines.flow.Flow

interface PendingWriteRepository {
    fun observePendingWrites(projectId: String): Flow<List<PendingWrite>>
    fun observeCountInOtherProjects(projectId: String): Flow<Int>
    suspend fun retry(id: Long)
    suspend fun discard(id: Long)
}
