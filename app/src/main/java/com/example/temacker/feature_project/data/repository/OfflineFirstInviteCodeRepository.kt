package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.data.database.InviteCodeDao
import com.example.temacker.core.data.firebase.toFirestoreDataError
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
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

    override fun observeActiveInviteCode(projectId: String): Flow<Result<InviteCode?, DataError>> = channelFlow {
        launch {
            remote.observeActiveInviteCode(projectId)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { code ->
                    // Firestore is the source of truth for which single code is active — always
                    // deactivate stale local rows first so Room's isActive=1 set never has more
                    // than the one Firestore just confirmed (old rows otherwise linger forever,
                    // since nothing else in Room deactivates them when a newer code is generated).
                    inviteCodeDao.deactivateAllForProject(projectId)
                    if (code != null) {
                        inviteCodeDao.upsert(code.toEntity())
                    }
                }
        }
        inviteCodeDao.observeActiveForProject(projectId).map { it?.toDomain() }.collect { send(Result.Success(it)) }
    }

    override suspend fun generateInviteCode(projectId: String) =
        remote.generateInviteCode(projectId).onSuccess {
            inviteCodeDao.deactivateAllForProject(projectId)
            inviteCodeDao.upsert(it.toEntity())
        }
}
