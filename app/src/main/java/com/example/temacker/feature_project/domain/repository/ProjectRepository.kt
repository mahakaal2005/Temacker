package com.example.temacker.feature_project.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeUserProjects(): Flow<Result<List<Project>, DataError>>
    // Waits for the server's first answer and caches it, so an empty cache is never mistaken for "no projects".
    suspend fun syncUserProjects(): EmptyResult<DataError>
    fun observeProject(projectId: String): Flow<Result<Project?, DataError>>
    suspend fun createProject(name: String, ownerDisplayName: String, ownerPhotoUrl: String?): Result<Project, DataError>

    // Archives oldProjectId and creates a new project inheriting its full roster. Caller must have
    // already verified the current user is oldProjectId's Leader — see TriggerSuccessionUseCase.
    suspend fun succeedProject(oldProjectId: String, newProjectName: String, leaderUid: String): Result<Project, DataError>
}
