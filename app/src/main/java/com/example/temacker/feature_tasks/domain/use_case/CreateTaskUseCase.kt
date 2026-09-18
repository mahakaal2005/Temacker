package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class CreateTaskUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        projectId: String,
        title: String,
        description: String?,
        dueDate: Long?,
        holderUid: String,
        holderDisplayName: String,
        createdByDisplayName: String
    ) = taskRepository.createTask(projectId, title, description, dueDate, holderUid, holderDisplayName, createdByDisplayName)
}
