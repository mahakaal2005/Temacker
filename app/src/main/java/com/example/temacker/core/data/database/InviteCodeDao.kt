package com.example.temacker.core.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InviteCodeDao {
    @Upsert
    suspend fun upsert(inviteCode: InviteCodeEntity)

    @Query("SELECT * FROM invite_codes WHERE projectId = :projectId AND isActive = 1 LIMIT 1")
    fun observeActiveForProject(projectId: String): Flow<InviteCodeEntity?>
}
