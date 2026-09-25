package com.example.temacker.core.domain.repository

import com.example.temacker.core.domain.model.ProjectExportData
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

// Cross-feature contract (architecture §8) — impl lives in feature_tasks/data (it owns Task/
// Handoff), bound in feature_tasks/di/TasksModule.kt. feature_profile's Your Data screen (Phase 6)
// depends on this interface only, same direction as TeamInsightsProvider.
interface ExportDataProvider {
    fun observeProjectExportData(projectId: String): Flow<Result<ProjectExportData, DataError>>
}
