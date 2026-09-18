package com.example.temacker.feature_project.data.repository

import androidx.room.withTransaction
import com.example.temacker.core.data.database.AppDatabase
import com.example.temacker.core.data.database.MembershipDao
import com.example.temacker.core.data.database.ProjectDao
import com.example.temacker.core.data.database.RoleDao
import com.example.temacker.core.data.firebase.toFirestoreDataError
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.feature_project.data.mapper.toDomain
import com.example.temacker.feature_project.data.mapper.toEntity
import com.example.temacker.feature_project.data.remote.ProjectRemoteDataSource
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.temacker.core.domain.util.map as resultMap

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstProjectRepository(
    private val remote: ProjectRemoteDataSource,
    private val appDatabase: AppDatabase,
    private val projectDao: ProjectDao,
    private val roleDao: RoleDao,
    private val membershipDao: MembershipDao,
    private val sessionManager: SessionManager
) : ProjectRepository {

    override fun observeUserProjects(): Flow<Result<List<Project>, DataError>> = channelFlow {
        val uid = sessionManager.getUid()
        if (uid == null) {
            send(Result.Success(emptyList()))
            return@channelFlow
        }

        launch {
            remote.observeUserProjects(uid)
                // Offline — the Room-backed emission below keeps serving cached rows regardless.
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { projects -> projectDao.upsertAll(projects.map { it.toEntity() }) }
        }

        membershipDao.observeByUser(uid)
            .map { rows -> rows.map { it.projectId } }
            .flatMapLatest { projectIds -> projectDao.observeActiveByIds(projectIds) }
            .map { entities -> entities.map { it.toDomain() } }
            .collect { send(Result.Success(it)) }

        awaitClose { }
    }

    override fun observeProject(projectId: String): Flow<Result<Project?, DataError>> = channelFlow {
        launch {
            remote.observeProject(projectId)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { project -> project?.let { projectDao.upsertAll(listOf(it.toEntity())) } }
        }

        projectDao.observeById(projectId).map { it?.toDomain() }.collect { send(Result.Success(it)) }
        awaitClose { }
    }

    override suspend fun createProject(
        name: String,
        ownerDisplayName: String,
        ownerPhotoUrl: String?
    ): Result<Project, DataError> {
        val uid = sessionManager.getUid() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return remote.createProjectWithLeader(name, uid, ownerDisplayName, ownerPhotoUrl)
            .onSuccess { (project, membership) ->
                projectDao.upsertAll(listOf(project.toEntity()))
                // Without this, observeUserProjects()'s Room-backed emission (driven by
                // membershipDao) never includes a project right after its own creation.
                membershipDao.upsertAll(listOf(membership.toEntity()))
            }
            .resultMap { (project, _) -> project }
    }

    override suspend fun succeedProject(
        oldProjectId: String,
        newProjectName: String,
        leaderUid: String
    ): Result<Project, DataError> =
        remote.succeedProject(oldProjectId, newProjectName, leaderUid)
            .onSuccess { result ->
                // One transaction so there's never a frame with zero non-archived projects for the user.
                appDatabase.withTransaction {
                    projectDao.upsertAll(listOf(result.newProject.toEntity()))
                    roleDao.upsertAll(result.newRoles.map { it.toEntity() })
                    membershipDao.upsertAll(result.newMemberships.map { it.toEntity() })
                    projectDao.archive(oldProjectId)
                }
            }
            .resultMap { it.newProject }
}
