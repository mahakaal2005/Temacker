package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MembershipDao {
    @Upsert
    suspend fun upsertAll(memberships: List<MembershipEntity>)

    @Query("SELECT * FROM memberships WHERE projectId = :projectId")
    fun observeByProject(projectId: String): Flow<List<MembershipEntity>>

    @Query("SELECT * FROM memberships WHERE userId = :userId")
    fun observeByUser(userId: String): Flow<List<MembershipEntity>>

    @Query("SELECT * FROM memberships WHERE projectId = :projectId AND userId = :userId")
    fun observeOne(projectId: String, userId: String): Flow<MembershipEntity?>

    @Query("DELETE FROM memberships WHERE projectId = :projectId AND userId = :userId")
    suspend fun delete(projectId: String, userId: String)

    @Query("DELETE FROM memberships WHERE projectId = :projectId AND userId NOT IN (:keepUserIds)")
    suspend fun deleteMissing(projectId: String, keepUserIds: List<String>)
}
