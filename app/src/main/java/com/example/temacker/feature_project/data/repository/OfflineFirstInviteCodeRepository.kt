package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.data.database.InviteCodeDao
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.feature_project.data.mapper.toDomain
import com.example.temacker.feature_project.data.mapper.toEntity
import com.example.temacker.feature_project.data.remote.InviteCodeRemoteDataSource
import com.example.temacker.feature_project.domain.model.InviteCode
import com.example.temacker.feature_project.domain.repository.InviteCodeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OfflineFirstInviteCodeRepository(
    private val remote: InviteCodeRemoteDataSource,
    private val inviteCodeDao: InviteCodeDao
) : InviteCodeRepository {

    override fun observeActiveInviteCode(projectId: String): Flow<InviteCode?> = channelFlow {
        launch {
            remote.observeActiveInviteCode(projectId)
                .catch { }
                .collect { code -> code?.let { inviteCodeDao.upsert(it.toEntity()) } }
        }
        inviteCodeDao.observeActiveForProject(projectId).map { it?.toDomain() }.collect { send(it) }
    }

    override suspend fun generateInviteCode(projectId: String) =
        remote.generateInviteCode(projectId).onSuccess { inviteCodeDao.upsert(it.toEntity()) }
}
