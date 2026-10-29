package com.example.temacker.feature_tasks.presentation.task_detail

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.Task

enum class RequestStage { NONE, CONFIRM_DROP, PICK_TRANSFER_TARGET }

@Stable
data class TaskDetailState(
    val task: Task? = null,
    val trail: List<Handoff> = emptyList(),
    val isCurrentUserHolder: Boolean = false,
    val canDelete: Boolean = false,
    val canReassign: Boolean = false,
    val members: List<com.example.temacker.core.domain.model.ProjectMember> = emptyList(),
    val isReassignVisible: Boolean = false,
    // Holders who can't approve requests themselves may ask the approver (the Leader) to take a task back or move it.
    val canRequestChange: Boolean = false,
    val approverName: String? = null,
    val hasPendingRequest: Boolean = false,
    val requestStage: RequestStage = RequestStage.NONE,
    val isMenuVisible: Boolean = false,
    val isDeleteConfirmVisible: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null
)
