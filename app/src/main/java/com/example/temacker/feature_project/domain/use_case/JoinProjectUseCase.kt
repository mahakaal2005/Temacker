package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.MembershipRepository

class JoinProjectUseCase(
    private val membershipRepository: MembershipRepository
) {
    suspend operator fun invoke(code: String, displayName: String, photoUrl: String?) =
        membershipRepository.joinProject(code, displayName, photoUrl)
}
