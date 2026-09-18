package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class AcceptHandoffUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(projectId: String, taskId: String, handoffId: String) =
        taskRepository.acceptHandoff(projectId, taskId, handoffId)
}
