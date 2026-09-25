package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.domain.model.ProjectRef
import com.example.temacker.core.domain.model.ProjectSummary
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.repository.SelectedProjectStore
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

// Adapter binding feature_project's real Project data to the core cross-feature contract
// (architecture §8) — feature_tasks depends on CurrentProjectProvider only, never on this class.
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectCurrentProjectProvider(
    private val projectRepository: ProjectRepository,
    private val membershipRepository: MembershipRepository,
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

    override fun observeCurrentProjectSummary(): Flow<Result<ProjectSummary?, DataError>> {
        return observeCurrentProjectId().flatMapLatest { idResult ->
            val id = (idResult as? Result.Success)?.data
            if (id == null) {
                val error = (idResult as? Result.Error)?.error
                flowOf(if (error != null) Result.Error(error) else Result.Success(null))
            } else {
                combine(
                    projectRepository.observeUserProjects(),
                    membershipRepository.observeMembers(id),
                    membershipRepository.observeMembership(id)
                ) { projectsResult, membersResult, membershipResult ->
                    toSummaryResult(id, projectsResult, membersResult, membershipResult)
                }
            }
        }
    }

    override fun observeUserProjectRefs(): Flow<Result<List<ProjectRef>, DataError>> =
        projectRepository.observeUserProjects().map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data.map { ProjectRef(it.id, it.name) })
                is Result.Error -> Result.Error(result.error)
            }
        }

    private fun toSummaryResult(
        id: String,
        projectsResult: Result<List<Project>, DataError>,
        membersResult: Result<List<Membership>, DataError>,
        membershipResult: Result<Membership?, DataError>
    ): Result<ProjectSummary?, DataError> {
        val projects = (projectsResult as? Result.Success)?.data
            ?: return Result.Error((projectsResult as Result.Error).error)
        val project = projects.firstOrNull { it.id == id } ?: return Result.Success(null)
        val memberCount = (membersResult as? Result.Success)?.data?.size ?: 0
        val roleName = (membershipResult as? Result.Success)?.data?.roleName.orEmpty()
        return Result.Success(
            ProjectSummary(
                name = project.name,
                memberCount = memberCount,
                roleName = roleName,
                hasOtherProjects = projects.size > 1
            )
        )
    }

    private data class Resolution(val selectedId: String?, val result: Result<String?, DataError>)
}
