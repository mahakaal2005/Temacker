package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.RoleRepository

class DeleteRoleUseCase(
    private val roleRepository: RoleRepository
) {
    suspend operator fun invoke(projectId: String, roleId: String) =
        roleRepository.deleteRole(projectId, roleId)
}
