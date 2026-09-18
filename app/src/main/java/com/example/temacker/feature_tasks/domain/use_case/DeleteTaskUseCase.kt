package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(projectId: String, taskId: String, byUid: String, byDisplayName: String) =
        taskRepository.deleteTask(projectId, taskId, byUid, byDisplayName)
}
