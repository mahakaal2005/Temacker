package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import kotlinx.coroutines.flow.first

class ReassignMemberRoleUseCase(
    private val membershipRepository: MembershipRepository
) {
    suspend operator fun invoke(projectId: String, userId: String, roleId: String): EmptyResult<DataError> {
        // The caller's own membership names who set the role.
        val me = (membershipRepository.observeMembership(projectId).first() as? Result.Success)?.data
            ?: return Result.Error(DataError.Network.PERMISSION_DENIED)
        return membershipRepository.reassignRole(projectId, userId, roleId, me.userId, me.displayName)
    }
}
