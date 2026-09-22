package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface MembershipRemoteDataSource {
    fun observeMembers(projectId: String): Flow<List<Membership>>
    fun observeMembership(projectId: String, userId: String): Flow<Membership?>
    // Also returns the joined Project so the repo can seed Room's project table immediately —
    // without it, observeUserProjects()'s Room-backed emission races the remote project listener
    // and can briefly still show only the old projects right after a join (see joinProject's caller).
    suspend fun joinProject(code: String, userId: String, displayName: String, photoUrl: String?): Result<Pair<Membership, Project>, DataError>
    suspend fun removeMember(projectId: String, userId: String): EmptyResult<DataError>
    suspend fun reassignRole(projectId: String, userId: String, roleId: String, byUid: String, byDisplayName: String): Result<Membership, DataError>
}
