package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.feature_project.domain.repository.InviteCodeRepository

class ObserveActiveInviteCodeUseCase(
    private val inviteCodeRepository: InviteCodeRepository
) {
    operator fun invoke(projectId: String) = inviteCodeRepository.observeActiveInviteCode(projectId)
}
