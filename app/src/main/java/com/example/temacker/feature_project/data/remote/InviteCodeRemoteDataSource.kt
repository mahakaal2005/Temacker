package com.example.temacker.feature_project.data.remote

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.InviteCode
import kotlinx.coroutines.flow.Flow

interface InviteCodeRemoteDataSource {
    fun observeActiveInviteCode(projectId: String): Flow<InviteCode?>
    suspend fun generateInviteCode(projectId: String, byUid: String, byDisplayName: String): Result<InviteCode, DataError>
}
