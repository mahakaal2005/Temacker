package com.example.temacker.feature_tasks.data.repository

import com.example.temacker.core.data.database.EventDao
import com.example.temacker.core.data.database.HandoffDao
import com.example.temacker.core.data.database.StuckHandoffRow
import com.example.temacker.core.data.database.TaskDao
import com.example.temacker.core.data.firebase.toFirestoreDataError
import com.example.temacker.core.domain.model.HolderLoad
import com.example.temacker.core.domain.model.StuckHandoff
import com.example.temacker.core.domain.model.TeamEvent
import com.example.temacker.core.domain.model.TeamEventType
import com.example.temacker.core.domain.repository.TeamInsightsProvider
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.data.mapper.toDomain
import com.example.temacker.feature_tasks.data.mapper.toEntity
import com.example.temacker.feature_tasks.data.remote.TaskRemoteDataSource
import com.example.temacker.feature_tasks.domain.model.Event
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Team screen (feature_project) reads Load/Stuck/Pulse through this — the reverse direction from
// ProjectMemberProvider/CurrentProjectProvider, since feature_tasks owns Task/Handoff/Event here.
// Reads Room DAOs directly rather than going through TaskRepository: the group-by-holder and
// joined stuck-handoff queries don't correspond to any single-entity repository method, and adding
// them to TaskRepository's public interface would leak Team-screen-specific shapes into the core
// task contract.
class TaskTeamInsightsProvider(
    private val remote: TaskRemoteDataSource,
    private val taskDao: TaskDao,
    private val handoffDao: HandoffDao,
    private val eventDao: EventDao
) : TeamInsightsProvider {

    override fun observeLoad(projectId: String): Flow<Result<List<HolderLoad>, DataError>> =
        taskDao.observeLoadByHolder(projectId).map { rows ->
            Result.Success(rows.map { HolderLoad(it.holderUid, it.holderDisplayName, it.todoCount, it.doingCount) })
        }

    override fun observeStuckHandoffs(projectId: String, thresholdMillis: Long): Flow<Result<List<StuckHandoff>, DataError>> =
        handoffDao.observeOfferedBefore(projectId, System.currentTimeMillis() - thresholdMillis).map { rows ->
            Result.Success(rows.map { it.toStuckHandoff() })
        }

    // Only Pulse needs event history, so its Room-sync side-effect lives here rather than in a
    // dedicated OfflineFirstEventRepository nobody else would use.
    override fun observePulse(projectId: String, limit: Int): Flow<Result<List<TeamEvent>, DataError>> = channelFlow {
        launch {
            remote.observeEvents(projectId, limit)
                .catch { e -> send(Result.Error(e.toFirestoreDataError())) }
                .collect { events ->
                    eventDao.upsertAll(events.map { it.toEntity() })
                    eventDao.deleteMissing(projectId, events.map { it.id })
                }
        }
        eventDao.observeByProject(projectId, limit)
            .map { entities -> entities.map { it.toDomain().toTeamEvent() } }
            .collect { send(Result.Success(it)) }
        awaitClose { }
    }
}

private fun StuckHandoffRow.toStuckHandoff(): StuckHandoff {
    val hoursStuck = (System.currentTimeMillis() - offeredAt) / (60 * 60 * 1000)
    return StuckHandoff(
        handoffId = id,
        taskId = taskId,
        taskTitle = taskTitle,
        fromDisplayName = fromDisplayName,
        toDisplayName = toDisplayName,
        offeredAt = offeredAt,
        hoursStuck = hoursStuck
    )
}

private fun Event.toTeamEvent(): TeamEvent = TeamEvent(
    id = id,
    type = TeamEventType.valueOf(type.name),
    taskTitle = taskTitle,
    byDisplayName = byDisplayName,
    at = at
)
