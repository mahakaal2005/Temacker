package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRemoteDataSource {
    fun observeUserProjects(userId: String): Flow<List<Project>>
    fun observeProject(projectId: String): Flow<Project?>

    // Creates the project, its immutable Leader role, an initial Default role, and the creator's
    // Leader membership as one atomic write — see architecture doc's Data Model / Firestore rules.
    suspend fun createProjectWithLeader(
        name: String,
        ownerUid: String,
        ownerDisplayName: String,
        ownerPhotoUrl: String?
    ): Result<Project, DataError>
}
