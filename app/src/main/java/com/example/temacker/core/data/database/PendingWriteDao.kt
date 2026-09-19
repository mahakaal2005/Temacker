package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingWriteDao {
    @Insert
    suspend fun insert(entity: PendingWriteEntity): Long

    @Query("SELECT * FROM pending_writes WHERE projectId = :projectId ORDER BY createdAt ASC, id ASC")
    fun observeByProject(projectId: String): Flow<List<PendingWriteEntity>>

    @Query("SELECT * FROM pending_writes WHERE status = 'PENDING' ORDER BY createdAt ASC, id ASC")
    suspend fun getPending(): List<PendingWriteEntity>

    @Query("SELECT COUNT(*) FROM pending_writes WHERE status = 'PENDING'")
    suspend fun countPending(): Int

    @Query("UPDATE pending_writes SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)

    @Query("UPDATE pending_writes SET status = 'FAILED', lastError = :reason WHERE id = :id")
    suspend fun markFailed(id: Long, reason: String)

    @Query("UPDATE pending_writes SET status = 'PENDING', attempts = 0, lastError = NULL WHERE id = :id")
    suspend fun retry(id: Long)

    @Query("DELETE FROM pending_writes WHERE id = :id")
    suspend fun delete(id: Long)
}
