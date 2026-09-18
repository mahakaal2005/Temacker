package com.example.temacker.core.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

// Cross-feature contract (architecture §8), same shape as ProjectMemberProvider — feature_tasks
// needs to know the user's current project id (today, "the first project the user belongs to",
// same logic every Phase 1 ViewModel already duplicates inline) without importing feature_project.
// Impl lives in feature_project/data, bound in feature_project/di/ProjectModule.kt.
interface CurrentProjectProvider {
    fun observeCurrentProjectId(): Flow<Result<String?, DataError>>
}
