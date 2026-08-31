package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.MembershipRepository

class RemoveMemberUseCase(
    private val membershipRepository: MembershipRepository
) {
    suspend operator fun invoke(projectId: String, userId: String) =
        membershipRepository.removeMember(projectId, userId)
}
