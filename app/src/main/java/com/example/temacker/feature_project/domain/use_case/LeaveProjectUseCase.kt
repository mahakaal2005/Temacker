package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.repository.MembershipRepository

// Removes the signed-in user's own membership. The Leader can't leave (the rules refuse it), so
// they have to transfer leadership first.
class LeaveProjectUseCase(
    private val membershipRepository: MembershipRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(projectId: String): EmptyResult<DataError> {
        val uid = sessionManager.getUid() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return membershipRepository.removeMember(projectId, uid)
    }
}
