package com.example.temacker.feature_profile.domain.use_case

import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import com.example.temacker.feature_profile.domain.model.DeleteAccountResult
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.domain.use_case.RemoveMemberUseCase
import kotlinx.coroutines.flow.first

// User-decided scope (Phase 6): delete the Firebase Auth user + leave every non-Leader project.
// A Leader anywhere blocks deletion entirely — Succession has to run first there, since the Leader
// membership can never be removed (Phase 1 rule, mirrored in firestore.rules).
class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase,
    private val removeMember: RemoveMemberUseCase,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): DeleteAccountResult {
        val uid = sessionManager.getUid() ?: return DeleteAccountResult.Failed(DataError.Network.UNAUTHORIZED)

        val projects = when (val result = observeUserProjects().first()) {
            is Result.Success -> result.data
            is Result.Error -> return DeleteAccountResult.Failed(result.error)
        }

        val leaderProjectNames = projects.filter { project ->
            (observeCurrentMembership(project.id).first() as? Result.Success)?.data?.isLeader == true
        }.map { it.name }
        if (leaderProjectNames.isNotEmpty()) return DeleteAccountResult.BlockedByLeadership(leaderProjectNames)

        projects.forEach { project ->
            val leaveResult = removeMember(project.id, uid)
            if (leaveResult is Result.Error) return DeleteAccountResult.Failed(leaveResult.error)
        }

        return when (val result = authRepository.deleteAccount()) {
            is Result.Success -> DeleteAccountResult.Success
            is Result.Error -> DeleteAccountResult.Failed(result.error)
        }
    }
}
