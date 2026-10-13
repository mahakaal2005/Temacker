package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class ReassignTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(projectId: String, taskId: String, toUid: String, toDisplayName: String, byDisplayName: String) =
        taskRepository.reassignTask(projectId, taskId, toUid, toDisplayName, byDisplayName)
}
