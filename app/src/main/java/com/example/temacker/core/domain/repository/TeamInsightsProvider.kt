package com.example.temacker.core.domain.repository

import com.example.temacker.core.domain.model.HolderLoad
import com.example.temacker.core.domain.model.StuckHandoff
import com.example.temacker.core.domain.model.TeamEvent
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

// Cross-feature contract (architecture §8) — impl lives in feature_tasks/data (it owns Task/Handoff/
// Event), bound in feature_tasks/di/TasksModule.kt. feature_project's Team screen depends on this
// interface only, the reverse direction from ProjectMemberProvider/CurrentProjectProvider.
interface TeamInsightsProvider {
    fun observeLoad(projectId: String): Flow<Result<List<HolderLoad>, DataError>>
    fun observeStuckHandoffs(projectId: String, thresholdMillis: Long = 24L * 60 * 60 * 1000): Flow<Result<List<StuckHandoff>, DataError>>
    fun observePulse(projectId: String, limit: Int = 50): Flow<Result<List<TeamEvent>, DataError>>
}
