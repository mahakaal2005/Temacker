package com.example.temacker.feature_project.data.repository

import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.core.domain.repository.ProjectMemberProvider
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.map
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Adapter binding feature_project's real Membership data to the core cross-feature contract
// (architecture §8) — feature_tasks depends on ProjectMemberProvider only, never on this class.
class MembershipProjectMemberProvider(
    private val membershipRepository: MembershipRepository
) : ProjectMemberProvider {

    override fun observeMembers(projectId: String): Flow<Result<List<ProjectMember>, DataError>> {
        return membershipRepository.observeMembers(projectId).map { result -> result.map { it.map { m -> m.toProjectMember() } } }
    }

    override fun observeCurrentMember(projectId: String): Flow<Result<ProjectMember?, DataError>> {
        return membershipRepository.observeMembership(projectId).map { result -> result.map { it?.toProjectMember() } }
    }
}

private fun Membership.toProjectMember(): ProjectMember = ProjectMember(
    uid = userId,
    displayName = displayName,
    photoUrl = photoUrl,
    roleName = roleName,
    canAssignTasks = permissions.assignTasks,
    canEditAnyTask = permissions.editAnyTask
)
