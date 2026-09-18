package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HandoffDao {
    @Upsert
    suspend fun upsertAll(handoffs: List<HandoffEntity>)

    @Query("SELECT * FROM handoffs WHERE taskId = :taskId ORDER BY offeredAt DESC")
    fun observeByTask(taskId: String): Flow<List<HandoffEntity>>

    @Query("SELECT * FROM handoffs WHERE projectId = :projectId AND toUid = :toUid AND status = 'OFFERED'")
    fun observePendingForUser(projectId: String, toUid: String): Flow<List<HandoffEntity>>

    @Query("DELETE FROM handoffs WHERE taskId = :taskId AND id NOT IN (:keepIds)")
    suspend fun deleteMissingForTask(taskId: String, keepIds: List<String>)

    @Query("DELETE FROM handoffs WHERE projectId = :projectId AND toUid = :toUid AND status = 'OFFERED' AND id NOT IN (:keepIds)")
    suspend fun deleteMissingPendingForUser(projectId: String, toUid: String, keepIds: List<String>)
}
