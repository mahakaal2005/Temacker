package com.example.temacker.feature_project.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Membership
import kotlinx.coroutines.flow.Flow

interface MembershipRepository {
    fun observeMembers(projectId: String): Flow<Result<List<Membership>, DataError>>
    // Membership of the currently signed-in user — uid is resolved internally via SessionManager.
    fun observeMembership(projectId: String): Flow<Result<Membership?, DataError>>
    suspend fun joinProject(code: String, displayName: String, photoUrl: String?): Result<Membership, DataError>
    suspend fun removeMember(projectId: String, userId: String): EmptyResult<DataError>
    suspend fun reassignRole(projectId: String, userId: String, roleId: String, byUid: String, byDisplayName: String): EmptyResult<DataError>
}
