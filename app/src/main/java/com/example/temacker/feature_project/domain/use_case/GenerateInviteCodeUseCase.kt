package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.InviteCodeRepository

class GenerateInviteCodeUseCase(
    private val inviteCodeRepository: InviteCodeRepository
) {
    suspend operator fun invoke(projectId: String) = inviteCodeRepository.generateInviteCode(projectId)
}
