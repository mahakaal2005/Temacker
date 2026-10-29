package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.import_tasks.ImportedTask
import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class ImportTasksUseCase(
    private val taskRepository: TaskRepository
) {
    // Returns how many were created and the first error, if any; stops at the first failure.
    suspend operator fun invoke(projectId: String, tasks: List<ImportedTask>, createdByDisplayName: String): Pair<Int, DataError?> {
        var created = 0
        for (task in tasks) {
            val result = taskRepository.createTask(
                projectId, task.title, task.description, task.dueDate,
                task.assignee.uid, task.assignee.displayName, createdByDisplayName
            )
            if (result is Result.Error) return created to result.error
            created++
        }
        return created to null
    }
}
