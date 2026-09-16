package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.data.database.RoleDao
import com.example.temacker.core.data.firebase.toFirestoreDataError
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.asEmptyResult
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.feature_project.data.mapper.toDomain
import com.example.temacker.feature_project.data.mapper.toEntity
import com.example.temacker.feature_project.data.remote.RoleRemoteDataSource
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.domain.repository.RoleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OfflineFirstRoleRepository(
    private val remote: RoleRemoteDataSource,
    private val roleDao: RoleDao
) : RoleRepository {

    override fun observeRoles(projectId: String): Flow<Result<List<Role>, DataError>> = channelFlow {
        launch {
            remote.observeRoles(projectId)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { roles -> roleDao.upsertAll(roles.map { it.toEntity() }) }
        }
        roleDao.observeByProject(projectId).map { it.map { e -> e.toDomain() } }.collect { send(Result.Success(it)) }
    }

    override suspend fun createRole(projectId: String, name: String, permissions: RolePermissions) =
        remote.createRole(projectId, name, permissions).onSuccess { roleDao.upsertAll(listOf(it.toEntity())) }

    override suspend fun updateRole(
        projectId: String,
        roleId: String,
        name: String,
        permissions: RolePermissions
    ) = remote.updateRole(projectId, roleId, name, permissions).onSuccess {
        roleDao.upsertAll(listOf(it.toEntity()))
    }.asEmptyResult()

    override suspend fun deleteRole(projectId: String, roleId: String) =
        remote.deleteRole(projectId, roleId).onSuccess { roleDao.deleteById(roleId) }
}
