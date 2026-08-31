package com.example.temacker.feature_project.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions
import kotlinx.coroutines.flow.Flow

interface RoleRepository {
    fun observeRoles(projectId: String): Flow<List<Role>>
    suspend fun createRole(projectId: String, name: String, permissions: RolePermissions): Result<Role, DataError>
    suspend fun updateRole(projectId: String, roleId: String, name: String, permissions: RolePermissions): EmptyResult<DataError>
    suspend fun deleteRole(projectId: String, roleId: String): EmptyResult<DataError>
}
