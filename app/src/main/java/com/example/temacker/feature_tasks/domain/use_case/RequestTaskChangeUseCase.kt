package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.feature_tasks.domain.model.HandoffKind
import com.example.temacker.feature_tasks.domain.repository.TaskRepository

class RequestTaskChangeUseCase(
    private val taskRepository: TaskRepository
) {
    // The approver is passed in (the Leader) so the request always lands with someone who can act on it.
    suspend operator fun invoke(projectId: String, taskId: String, kind: HandoffKind, approver: ProjectMember, target: ProjectMember?, note: String?) =
        taskRepository.requestTaskChange(projectId, taskId, kind, approver.uid, approver.displayName, target?.uid, target?.displayName, note)
}
