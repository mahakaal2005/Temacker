package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Membership
import kotlinx.coroutines.flow.Flow

interface MembershipRemoteDataSource {
    fun observeMembers(projectId: String): Flow<List<Membership>>
    fun observeMembership(projectId: String, userId: String): Flow<Membership?>
    suspend fun joinProject(code: String, userId: String, displayName: String, photoUrl: String?): Result<Membership, DataError>
    suspend fun removeMember(projectId: String, userId: String): EmptyResult<DataError>
    suspend fun reassignRole(projectId: String, userId: String, roleId: String, byUid: String, byDisplayName: String): Result<Membership, DataError>
}
