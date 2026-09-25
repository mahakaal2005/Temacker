package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.model.SuccessionResult
import kotlinx.coroutines.flow.Flow

// isAuthoritative is false for cache-served snapshots, which may lag the server and must never delete local rows.
data class UserProjectsSnapshot(
    val projects: List<Project>,
    val memberships: List<Membership>,
    val isAuthoritative: Boolean
)

interface ProjectRemoteDataSource {
    fun observeUserProjects(userId: String): Flow<UserProjectsSnapshot>
    fun observeProject(projectId: String): Flow<Project?>

    // Creates the project, its immutable Leader role, an initial Default role, and the creator's
    // Leader membership as one atomic write — see architecture doc's Data Model / Firestore rules.
    // Returns the Leader membership alongside the project so the caller can seed the local Room
    // cache immediately, the same way joinProject() does — without it, observeUserProjects()'s
    // Room-backed emission never includes a project right after its own creation.
    suspend fun createProjectWithLeader(
        name: String,
        ownerUid: String,
        ownerDisplayName: String,
        ownerPhotoUrl: String?
    ): Result<Pair<Project, Membership>, DataError>

    // Succession: archives oldProjectId and creates a new project inheriting its full roster
    // (roles + memberships, remapped to new IDs). leaderUid becomes the new project's owner —
    // caller (TriggerSuccessionUseCase) has already verified they're the old project's Leader.
    suspend fun succeedProject(
        oldProjectId: String,
        newProjectName: String,
        leaderUid: String
    ): Result<SuccessionResult, DataError>
}
