package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Upsert
    suspend fun upsertAll(events: List<EventEntity>)

    @Query("SELECT * FROM events WHERE projectId = :projectId ORDER BY at DESC LIMIT :limit")
    fun observeByProject(projectId: String, limit: Int): Flow<List<EventEntity>>

    @Query("DELETE FROM events WHERE projectId = :projectId AND id NOT IN (:keepIds)")
    suspend fun deleteMissing(projectId: String, keepIds: List<String>)
}
