package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.domain.repository.RoleRepository

class CreateRoleUseCase(
    private val roleRepository: RoleRepository
) {
    suspend operator fun invoke(projectId: String, name: String, permissions: RolePermissions) =
        roleRepository.createRole(projectId, name, permissions)
}
