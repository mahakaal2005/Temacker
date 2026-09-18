package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Upsert
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun observeByProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeOne(taskId: String): Flow<TaskEntity?>

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun delete(taskId: String)

    @Query("DELETE FROM tasks WHERE projectId = :projectId AND id NOT IN (:keepIds)")
    suspend fun deleteMissing(projectId: String, keepIds: List<String>)
}
