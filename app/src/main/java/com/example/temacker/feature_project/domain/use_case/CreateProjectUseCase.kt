package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.ProjectRepository

class CreateProjectUseCase(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(name: String, ownerDisplayName: String, ownerPhotoUrl: String?) =
        projectRepository.createProject(name, ownerDisplayName, ownerPhotoUrl)
}
