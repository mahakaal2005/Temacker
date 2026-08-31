package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.domain.repository.RoleRepository

class UpdateRoleUseCase(
    private val roleRepository: RoleRepository
) {
    suspend operator fun invoke(projectId: String, roleId: String, name: String, permissions: RolePermissions) =
        roleRepository.updateRole(projectId, roleId, name, permissions)
}
