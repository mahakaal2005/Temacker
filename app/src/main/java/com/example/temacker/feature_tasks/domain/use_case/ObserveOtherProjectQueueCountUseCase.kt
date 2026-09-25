package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.PendingWriteRepository

class ObserveOtherProjectQueueCountUseCase(
    private val repository: PendingWriteRepository
) {
    operator fun invoke(projectId: String) = repository.observeCountInOtherProjects(projectId)
}
