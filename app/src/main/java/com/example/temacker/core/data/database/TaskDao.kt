package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class HolderLoadRow(
    val holderUid: String,
    val holderDisplayName: String,
    val todoCount: Int,
    val doingCount: Int
)

@Dao
interface TaskDao {
    @Upsert
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun observeByProject(projectId: String): Flow<List<TaskEntity>>

    // Team "Load" tab — who's holding what, grouped by holder.
    @Query(
        """
        SELECT holderUid, holderDisplayName,
            SUM(CASE WHEN status = 'TODO' THEN 1 ELSE 0 END) AS todoCount,
            SUM(CASE WHEN status = 'DOING' THEN 1 ELSE 0 END) AS doingCount
        FROM tasks
        WHERE projectId = :projectId AND status != 'DONE'
        GROUP BY holderUid, holderDisplayName
        """
    )
    fun observeLoadByHolder(projectId: String): Flow<List<HolderLoadRow>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeOne(taskId: String): Flow<TaskEntity?>

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun delete(taskId: String)

    @Query("DELETE FROM tasks WHERE projectId = :projectId AND id NOT IN (:keepIds)")
    suspend fun deleteMissing(projectId: String, keepIds: List<String>)
}
