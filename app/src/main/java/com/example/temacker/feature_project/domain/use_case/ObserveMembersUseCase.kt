package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.MembershipRepository

class ObserveMembersUseCase(
    private val membershipRepository: MembershipRepository
) {
    operator fun invoke(projectId: String) = membershipRepository.observeMembers(projectId)
}
