package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.InviteCode
import com.example.temacker.feature_project.domain.repository.InviteCodeRepository
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import kotlinx.coroutines.flow.first

class GenerateInviteCodeUseCase(
    private val inviteCodeRepository: InviteCodeRepository,
    private val membershipRepository: MembershipRepository
) {
    suspend operator fun invoke(projectId: String): Result<InviteCode, DataError> {
        // The caller's own membership names who made the code, so joiners can see who added them.
        val me = (membershipRepository.observeMembership(projectId).first() as? Result.Success)?.data
            ?: return Result.Error(DataError.Network.PERMISSION_DENIED)
        return inviteCodeRepository.generateInviteCode(projectId, me.userId, me.displayName)
    }
}
