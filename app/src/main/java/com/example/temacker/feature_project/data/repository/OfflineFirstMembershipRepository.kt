package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.data.database.MembershipDao
import com.example.temacker.core.data.firebase.toFirestoreDataError
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.feature_project.data.mapper.toDomain
import com.example.temacker.feature_project.data.mapper.toEntity
import com.example.temacker.feature_project.data.remote.MembershipRemoteDataSource
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.temacker.core.domain.util.asEmptyResult

class OfflineFirstMembershipRepository(
    private val remote: MembershipRemoteDataSource,
    private val membershipDao: MembershipDao,
    private val sessionManager: SessionManager
) : MembershipRepository {

    override fun observeMembers(projectId: String): Flow<Result<List<Membership>, DataError>> = channelFlow {
        launch {
            remote.observeMembers(projectId)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { members ->
                    membershipDao.upsertAll(members.map { it.toEntity() })
                    membershipDao.deleteMissing(projectId, members.map { it.userId })
                }
        }
        membershipDao.observeByProject(projectId).map { it.map { e -> e.toDomain() } }.collect { send(Result.Success(it)) }
    }

    override fun observeMembership(projectId: String): Flow<Result<Membership?, DataError>> = channelFlow {
        val uid = sessionManager.getUid()
        if (uid == null) {
            send(Result.Success(null))
            return@channelFlow
        }
        launch {
            remote.observeMembership(projectId, uid)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { membership ->
                    if (membership != null) {
                        membershipDao.upsertAll(listOf(membership.toEntity()))
                    } else {
                        membershipDao.delete(projectId, uid)
                    }
                }
        }
        membershipDao.observeOne(projectId, uid).map { it?.toDomain() }.collect { send(Result.Success(it)) }
    }

    override suspend fun joinProject(code: String, displayName: String, photoUrl: String?): Result<Membership, DataError> {
        val uid = sessionManager.getUid() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return remote.joinProject(code, uid, displayName, photoUrl)
            .onSuccess { membershipDao.upsertAll(listOf(it.toEntity())) }
    }

    override suspend fun removeMember(projectId: String, userId: String) =
        remote.removeMember(projectId, userId).onSuccess { membershipDao.delete(projectId, userId) }

    override suspend fun reassignRole(projectId: String, userId: String, roleId: String) =
        remote.reassignRole(projectId, userId, roleId)
            .onSuccess { membershipDao.upsertAll(listOf(it.toEntity())) }
            .asEmptyResult()
}
