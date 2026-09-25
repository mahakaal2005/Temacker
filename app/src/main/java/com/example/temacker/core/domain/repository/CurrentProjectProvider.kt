package com.example.temacker.core.domain.repository

import com.example.temacker.core.domain.model.ProjectRef
import com.example.temacker.core.domain.model.ProjectSummary
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

// Cross-feature contract (architecture §8), same shape as ProjectMemberProvider — feature_tasks
// needs to know the user's current project id (today, "the first project the user belongs to",
// same logic every Phase 1 ViewModel already duplicates inline) without importing feature_project.
// Impl lives in feature_project/data, bound in feature_project/di/ProjectModule.kt.
interface CurrentProjectProvider {
    fun observeCurrentProjectId(): Flow<Result<String?, DataError>>

    // Phase 5b, Board/Inbox switcher pill: name + member count + the viewer's role in the current
    // project, trimmed to what the pill needs (architecture §8 — core can't see feature_project's
    // Project/Membership). Null once observeUserProjects() is empty (no project at all).
    fun observeCurrentProjectSummary(): Flow<Result<ProjectSummary?, DataError>>

    // Phase 7, cross-project Inbox and badge: every non-archived project the user belongs to.
    fun observeUserProjectRefs(): Flow<Result<List<ProjectRef>, DataError>>
}
