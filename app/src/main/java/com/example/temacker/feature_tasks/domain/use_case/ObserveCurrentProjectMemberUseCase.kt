package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.repository.ProjectMemberProvider

class ObserveCurrentProjectMemberUseCase(
    private val projectMemberProvider: ProjectMemberProvider
) {
    operator fun invoke(projectId: String) = projectMemberProvider.observeCurrentMember(projectId)
}
