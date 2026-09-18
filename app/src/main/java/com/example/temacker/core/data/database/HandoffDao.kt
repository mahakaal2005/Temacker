package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class StuckHandoffRow(
    val id: String,
    val taskId: String,
    val taskTitle: String,
    val fromDisplayName: String,
    val toDisplayName: String,
    val offeredAt: Long
)

@Dao
interface HandoffDao {
    @Upsert
    suspend fun upsertAll(handoffs: List<HandoffEntity>)

    @Query("SELECT * FROM handoffs WHERE taskId = :taskId ORDER BY offeredAt DESC")
    fun observeByTask(taskId: String): Flow<List<HandoffEntity>>

    // Team "Stuck" tab — offered but unanswered past a threshold, joined against tasks for the title.
    @Query(
        """
        SELECT h.id AS id, h.taskId AS taskId, t.title AS taskTitle,
            h.fromDisplayName AS fromDisplayName, h.toDisplayName AS toDisplayName, h.offeredAt AS offeredAt
        FROM handoffs h
        INNER JOIN tasks t ON t.id = h.taskId
        WHERE h.projectId = :projectId AND h.status = 'OFFERED' AND h.offeredAt < :cutoffMillis
        ORDER BY h.offeredAt ASC
        """
    )
    fun observeOfferedBefore(projectId: String, cutoffMillis: Long): Flow<List<StuckHandoffRow>>

    @Query("SELECT * FROM handoffs WHERE projectId = :projectId AND toUid = :toUid AND status = 'OFFERED'")
    fun observePendingForUser(projectId: String, toUid: String): Flow<List<HandoffEntity>>

    @Query("DELETE FROM handoffs WHERE taskId = :taskId AND id NOT IN (:keepIds)")
    suspend fun deleteMissingForTask(taskId: String, keepIds: List<String>)

    @Query("DELETE FROM handoffs WHERE projectId = :projectId AND toUid = :toUid AND status = 'OFFERED' AND id NOT IN (:keepIds)")
    suspend fun deleteMissingPendingForUser(projectId: String, toUid: String, keepIds: List<String>)
}
