package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class OfferHandoffUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(projectId: String, taskId: String, toUid: String, toDisplayName: String, note: String?) =
        taskRepository.offerHandoff(projectId, taskId, toUid, toDisplayName, note)
}
