package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class ObserveHandoffTrailUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(projectId: String, taskId: String) = taskRepository.observeHandoffTrail(projectId, taskId)
}
