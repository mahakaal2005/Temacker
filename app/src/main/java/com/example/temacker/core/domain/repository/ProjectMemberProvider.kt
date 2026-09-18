package com.example.temacker.core.domain.repository

import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

// Cross-feature contract (architecture §8) — impl lives in feature_project/data, bound in
// feature_project/di/ProjectModule.kt. feature_tasks depends on this interface only.
interface ProjectMemberProvider {
    fun observeMembers(projectId: String): Flow<Result<List<ProjectMember>, DataError>>
    fun observeCurrentMember(projectId: String): Flow<Result<ProjectMember?, DataError>>
}
