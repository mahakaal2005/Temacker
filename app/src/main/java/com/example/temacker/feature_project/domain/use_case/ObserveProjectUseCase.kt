package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.ProjectRepository

class ObserveProjectUseCase(
    private val projectRepository: ProjectRepository
) {
    operator fun invoke(projectId: String) = projectRepository.observeProject(projectId)
}
