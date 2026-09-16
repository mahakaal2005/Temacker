package com.example.temacker.feature_project.domain.repository

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.InviteCode
import kotlinx.coroutines.flow.Flow

interface InviteCodeRepository {
    fun observeActiveInviteCode(projectId: String): Flow<Result<InviteCode?, DataError>>
    suspend fun generateInviteCode(projectId: String): Result<InviteCode, DataError>
}
