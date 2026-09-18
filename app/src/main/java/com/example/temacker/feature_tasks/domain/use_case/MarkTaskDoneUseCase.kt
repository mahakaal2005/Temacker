package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class MarkTaskDoneUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(projectId: String, taskId: String) =
        taskRepository.markTaskDone(projectId, taskId)
}
