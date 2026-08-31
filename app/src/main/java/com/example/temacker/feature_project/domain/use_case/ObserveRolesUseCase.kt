package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.RoleRepository

class ObserveRolesUseCase(
    private val roleRepository: RoleRepository
) {
    operator fun invoke(projectId: String) = roleRepository.observeRoles(projectId)
}
