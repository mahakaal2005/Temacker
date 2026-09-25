package com.example.temacker.feature_profile.domain.use_case

import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.repository.ExportDataProvider
import com.example.temacker.core.domain.repository.ProjectMemberProvider
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_profile.domain.model.ProjectExportSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull

// Shared by Your Data and Plan & Limits (Phase 6) — both just need counts, one needs the full rows too.
@OptIn(ExperimentalCoroutinesApi::class)
class GetProjectExportUseCase(
    private val currentProjectProvider: CurrentProjectProvider,
    private val exportDataProvider: ExportDataProvider,
    private val projectMemberProvider: ProjectMemberProvider
) {
    operator fun invoke(): Flow<Result<ProjectExportSnapshot, DataError>> {
        val projectId = currentProjectProvider.observeCurrentProjectId()
            .mapNotNull { (it as? Result.Success)?.data }
            .distinctUntilChanged()

        return projectId.flatMapLatest { id ->
            combine(
                currentProjectProvider.observeCurrentProjectSummary(),
                exportDataProvider.observeProjectExportData(id),
                projectMemberProvider.observeMembers(id)
            ) { summaryResult, exportResult, membersResult ->
                val summary = (summaryResult as? Result.Success)?.data
                val export = (exportResult as? Result.Success)?.data
                val members = (membersResult as? Result.Success)?.data
                if (summary != null && export != null && members != null) {
                    Result.Success(ProjectExportSnapshot(summary.name, export, members))
                } else {
                    val error = listOf(summaryResult, exportResult, membersResult)
                        .filterIsInstance<Result.Error<DataError>>()
                        .firstOrNull()?.error ?: DataError.Local.UNKNOWN
                    Result.Error(error)
                }
            }
        }
    }
}
