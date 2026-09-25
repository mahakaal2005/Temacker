package com.example.temacker.feature_tasks.data.repository

import com.example.temacker.core.data.database.HandoffDao
import com.example.temacker.core.data.database.HandoffEntity
import com.example.temacker.core.data.database.TaskDao
import com.example.temacker.core.data.database.TaskEntity
import com.example.temacker.core.domain.model.ExportHandoff
import com.example.temacker.core.domain.model.ExportTask
import com.example.temacker.core.domain.model.ProjectExportData
import com.example.temacker.core.domain.repository.ExportDataProvider
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

// Your Data screen (feature_profile, Phase 6) reads project-wide tasks+handoffs through this — the
// same reverse direction as TeamInsightsProvider. Reads Room DAOs directly rather than going through
// TaskRepository: a project-wide handoff read doesn't correspond to any existing repository method
// (observeHandoffTrail is per-task), matching TaskTeamInsightsProvider's precedent for not leaking
// screen-specific shapes into the core task contract.
class TaskExportDataProvider(
    private val taskDao: TaskDao,
    private val handoffDao: HandoffDao
) : ExportDataProvider {

    override fun observeProjectExportData(projectId: String): Flow<Result<ProjectExportData, DataError>> =
        combine(taskDao.observeByProject(projectId), handoffDao.observeByProject(projectId)) { tasks, handoffs ->
            Result.Success(
                ProjectExportData(
                    tasks = tasks.map { it.toExportTask() },
                    handoffs = handoffs.map { it.handoff.toExportHandoff(it.taskTitle) }
                )
            )
        }
}

private fun TaskEntity.toExportTask() = ExportTask(
    id = id,
    title = title,
    status = status,
    holderDisplayName = holderDisplayName,
    createdByDisplayName = createdByDisplayName,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun HandoffEntity.toExportHandoff(taskTitle: String) = ExportHandoff(
    id = id,
    taskTitle = taskTitle,
    fromDisplayName = fromDisplayName,
    toDisplayName = toDisplayName,
    note = note,
    status = status,
    declineReason = declineReason,
    offeredAt = offeredAt,
    respondedAt = respondedAt
)
