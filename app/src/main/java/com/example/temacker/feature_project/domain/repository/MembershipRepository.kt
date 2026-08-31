package com.example.temacker.feature_project.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Membership
import kotlinx.coroutines.flow.Flow

interface MembershipRepository {
    fun observeMembers(projectId: String): Flow<List<Membership>>
    // Membership of the currently signed-in user — uid is resolved internally via SessionManager.
    fun observeMembership(projectId: String): Flow<Membership?>
    suspend fun joinProject(code: String, displayName: String, photoUrl: String?): Result<Membership, DataError>
    suspend fun removeMember(projectId: String, userId: String): EmptyResult<DataError>
    suspend fun reassignRole(projectId: String, userId: String, roleId: String): EmptyResult<DataError>
}
