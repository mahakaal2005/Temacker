package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.ProjectRepository

class SyncUserProjectsUseCase(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke() = projectRepository.syncUserProjects()
}
