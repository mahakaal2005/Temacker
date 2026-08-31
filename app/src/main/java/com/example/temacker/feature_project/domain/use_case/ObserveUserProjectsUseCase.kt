package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.ProjectRepository

class ObserveUserProjectsUseCase(
    private val projectRepository: ProjectRepository
) {
    operator fun invoke() = projectRepository.observeUserProjects()
}
