package com.example.temacker.feature_tasks.data.repository

import com.example.temacker.core.data.database.PendingWriteDao
import com.example.temacker.feature_tasks.data.mapper.toDomain
import com.example.temacker.feature_tasks.data.worker.PendingWriteScheduler
import com.example.temacker.feature_tasks.domain.model.PendingWrite
import com.example.temacker.feature_tasks.domain.repository.PendingWriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstPendingWriteRepository(
    private val dao: PendingWriteDao,
    private val scheduler: PendingWriteScheduler
) : PendingWriteRepository {

    override fun observePendingWrites(projectId: String): Flow<List<PendingWrite>> =
        dao.observeByProject(projectId).map { rows -> rows.mapNotNull { it.toDomain() } }

    override fun observeCountInOtherProjects(projectId: String): Flow<Int> = dao.observeCountOutsideProject(projectId)

    override suspend fun retry(id: Long) {
        dao.retry(id)
        scheduler.enqueue()
    }

    override suspend fun discard(id: Long) = dao.delete(id)
}
