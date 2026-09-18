package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class ObserveTaskUseCase(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(projectId: String, taskId: String) = taskRepository.observeTask(projectId, taskId)
}
