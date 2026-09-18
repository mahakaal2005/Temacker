package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.first

class TriggerSuccessionUseCase(
    private val projectRepository: ProjectRepository,
    private val membershipRepository: MembershipRepository,
    private val sessionManager: SessionManager
) {
    // Use-case-level Leader guard — the actual permission enforcement lives in Firestore rules
    // (hasLeaderRole), this just fails fast without a round trip for a non-Leader caller.
    suspend operator fun invoke(projectId: String, newProjectName: String): Result<Project, DataError> {
        val uid = sessionManager.getUid() ?: return Result.Error(DataError.Network.UNAUTHORIZED)
        return when (val membershipResult = membershipRepository.observeMembership(projectId).first()) {
            is Result.Error -> Result.Error(membershipResult.error)
            is Result.Success -> {
                val membership = membershipResult.data
                if (membership == null || !membership.isLeader) {
                    Result.Error(DataError.Network.FORBIDDEN)
                } else {
                    projectRepository.succeedProject(projectId, newProjectName, uid)
                }
            }
        }
    }
}
