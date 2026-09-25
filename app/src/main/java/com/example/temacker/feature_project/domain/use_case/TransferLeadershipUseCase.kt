package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import kotlinx.coroutines.flow.first

// Leader-only: hands the Leader role to another member, who becomes Leader while the caller drops to
// the project's default role. The rules enforce the same pairing server-side.
class TransferLeadershipUseCase(
    private val membershipRepository: MembershipRepository
) {
    suspend operator fun invoke(projectId: String, toUserId: String): EmptyResult<DataError> {
        val me = (membershipRepository.observeMembership(projectId).first() as? Result.Success)?.data
            ?: return Result.Error(DataError.Network.PERMISSION_DENIED)
        if (!me.isLeader || me.userId == toUserId) return Result.Error(DataError.Network.PERMISSION_DENIED)
        return membershipRepository.transferLeadership(projectId, me.userId, me.displayName, toUserId)
    }
}
