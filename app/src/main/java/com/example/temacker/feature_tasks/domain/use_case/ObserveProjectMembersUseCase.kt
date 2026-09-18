package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.repository.ProjectMemberProvider

class ObserveProjectMembersUseCase(
    private val projectMemberProvider: ProjectMemberProvider
) {
    operator fun invoke(projectId: String) = projectMemberProvider.observeMembers(projectId)
}
