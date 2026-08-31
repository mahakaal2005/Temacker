package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.data.database.MembershipDao
import com.example.temacker.core.data.database.ProjectDao
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

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFirstProjectRepository(
    private val remote: ProjectRemoteDataSource,
    private val projectDao: ProjectDao,
    private val membershipDao: MembershipDao,
    private val sessionManager: SessionManager
) : ProjectRepository {

    override fun observeUserProjects(): Flow<List<Project>> = channelFlow {
        val uid = sessionManager.getUid()
        if (uid == null) {
            send(emptyList())
            return@channelFlow
        }

        launch {
            remote.observeUserProjects(uid)
                .catch { } // offline — the Room-backed emission below keeps serving cached rows.
                .collect { projects -> projectDao.upsertAll(projects.map { it.toEntity() }) }
        }

        membershipDao.observeByUser(uid)
            .map { rows -> rows.map { it.projectId } }
            .flatMapLatest { projectIds -> projectDao.observeByIds(projectIds) }
            .map { entities -> entities.map { it.toDomain() } }
            .collect { send(it) }

        awaitClose { }
    }

    override fun observeProject(projectId: String): Flow<Project?> = channelFlow {
        launch {
            remote.observeProject(projectId)
                .catch { }
                .collect { project -> project?.let { projectDao.upsertAll(listOf(it.toEntity())) } }
        }

        projectDao.observeById(projectId).map { it?.toDomain() }.collect { send(it) }
        awaitClose { }
    }

    override suspend fun createProject(
        name: String,
        ownerDisplayName: String,
        ownerPhotoUrl: String?
    ): Result<Project, DataError> {
        val uid = sessionManager.getUid() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return remote.createProjectWithLeader(name, uid, ownerDisplayName, ownerPhotoUrl)
            .onSuccess { project -> projectDao.upsertAll(listOf(project.toEntity())) }
    }
}
