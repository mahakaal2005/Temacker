package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.map
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Adapter binding feature_project's real Project data to the core cross-feature contract
// (architecture §8) — feature_tasks depends on CurrentProjectProvider only, never on this class.
class ProjectCurrentProjectProvider(
    private val projectRepository: ProjectRepository
) : CurrentProjectProvider {

    override fun observeCurrentProjectId(): Flow<Result<String?, DataError>> {
        return projectRepository.observeUserProjects().map { result -> result.map { it.firstOrNull()?.id } }
    }
}
