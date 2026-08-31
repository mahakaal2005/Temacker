package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.MembershipRepository

class ReassignMemberRoleUseCase(
    private val membershipRepository: MembershipRepository
) {
    suspend operator fun invoke(projectId: String, userId: String, roleId: String) =
        membershipRepository.reassignRole(projectId, userId, roleId)
}
