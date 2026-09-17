package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {
    @Upsert
    suspend fun upsertAll(roles: List<RoleEntity>)

    @Query("SELECT * FROM roles WHERE projectId = :projectId")
    fun observeByProject(projectId: String): Flow<List<RoleEntity>>

    @Query("DELETE FROM roles WHERE id = :roleId")
    suspend fun deleteById(roleId: String)

    @Query("DELETE FROM roles WHERE projectId = :projectId AND id NOT IN (:keepIds)")
    suspend fun deleteMissing(projectId: String, keepIds: List<String>)
}
