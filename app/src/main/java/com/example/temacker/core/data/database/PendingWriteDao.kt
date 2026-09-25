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

    // Queue rows in projects other than the selected one, so a stuck write there is never invisible.
    @Query("SELECT COUNT(*) FROM pending_writes WHERE projectId != :projectId")
    fun observeCountOutsideProject(projectId: String): Flow<Int>

    @Query("SELECT * FROM pending_writes WHERE status = 'PENDING' ORDER BY createdAt ASC, id ASC")
    suspend fun getPending(): List<PendingWriteEntity>

    @Query("SELECT COUNT(*) FROM pending_writes WHERE status = 'PENDING'")
    suspend fun countPending(): Int

    @Query("SELECT COUNT(*) FROM pending_writes WHERE status = 'PENDING' AND type = :type AND taskId = :taskId")
    suspend fun countPendingFor(type: String, taskId: String): Int

    // Tasks whose offer already has a queued accept or decline, so it no longer counts as waiting.
    @Query("SELECT taskId FROM pending_writes WHERE projectId = :projectId AND status = 'PENDING' AND type IN ('ACCEPT', 'DECLINE')")
    fun observeAnsweredTaskIds(projectId: String): Flow<List<String>>

    @Query("UPDATE pending_writes SET attempts = attempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: Long)

    @Query("UPDATE pending_writes SET status = 'FAILED', lastError = :reason WHERE id = :id")
    suspend fun markFailed(id: Long, reason: String)

    @Query("UPDATE pending_writes SET status = 'PENDING', attempts = 0, lastError = NULL WHERE id = :id")
    suspend fun retry(id: Long)

    @Query("DELETE FROM pending_writes WHERE id = :id")
    suspend fun delete(id: Long)
}
