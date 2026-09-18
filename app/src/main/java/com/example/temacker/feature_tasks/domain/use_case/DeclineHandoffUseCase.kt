package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class DeclineHandoffUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(projectId: String, taskId: String, handoffId: String, reason: String) =
        taskRepository.declineHandoff(projectId, taskId, handoffId, reason)
}
