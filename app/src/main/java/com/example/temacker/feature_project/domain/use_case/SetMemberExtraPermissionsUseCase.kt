package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.EmptyResult
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.domain.repository.MembershipRepository

class SetMemberExtraPermissionsUseCase(
    private val membershipRepository: MembershipRepository
) {
    // deleteProject is never grantable as an extra.
    suspend operator fun invoke(projectId: String, userId: String, extras: RolePermissions): EmptyResult<DataError> =
        membershipRepository.setExtraPermissions(projectId, userId, extras.copy(deleteProject = false))
}
