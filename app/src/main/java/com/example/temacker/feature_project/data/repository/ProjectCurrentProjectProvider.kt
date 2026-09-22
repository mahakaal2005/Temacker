package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.repository.SelectedProjectStore
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

// Adapter binding feature_project's real Project data to the core cross-feature contract
// (architecture §8) — feature_tasks depends on CurrentProjectProvider only, never on this class.
class ProjectCurrentProjectProvider(
    private val projectRepository: ProjectRepository,
    private val selectedProjectStore: SelectedProjectStore
) : CurrentProjectProvider {

    override fun observeCurrentProjectId(): Flow<Result<String?, DataError>> {
        return combine(
            selectedProjectStore.observeSelectedProjectId(),
            projectRepository.observeUserProjects()
        ) { selectedId, projectsResult ->
            when (projectsResult) {
                is Result.Success -> {
                    val projects = projectsResult.data
                    val resolvedId = projects.firstOrNull { it.id == selectedId }?.id
                        ?: projects.firstOrNull()?.id
                    Resolution(selectedId, Result.Success(resolvedId))
                }
                is Result.Error -> Resolution(selectedId, Result.Error(projectsResult.error))
            }
        }.onEach { resolution ->
            // A stale or missing selection resolves to a different id — persist it so it self-corrects.
            val resolvedId = (resolution.result as? Result.Success)?.data
            if (resolvedId != null && resolvedId != resolution.selectedId) {
                selectedProjectStore.setSelectedProjectId(resolvedId)
            }
        }.map { it.result }
    }

    private data class Resolution(val selectedId: String?, val result: Result<String?, DataError>)
}
