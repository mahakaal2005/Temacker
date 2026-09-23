package com.example.temacker.feature_tasks.presentation.inbox

import androidx.compose.runtime.Stable
import com.example.temacker.core.domain.model.ProjectSummary
import com.example.temacker.core.presentation.util.UiText

enum class InboxRowKind { OFFER_TO_YOU, UNANSWERED, ACCEPTED, DECLINED }

data class InboxRowUi(
    val handoffId: String,
    val taskId: String,
    val title: String,
    val detail: String,
    val kind: InboxRowKind
)

@Stable
data class InboxState(
    val projectSummary: ProjectSummary? = null,
    val waiting: List<InboxRowUi> = emptyList(),
    val earlier: List<InboxRowUi> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null
)
