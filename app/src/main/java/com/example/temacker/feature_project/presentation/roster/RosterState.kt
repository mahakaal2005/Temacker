package com.example.temacker.feature_project.presentation.roster

import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Role

data class RosterState(
    val projectId: String? = null,
    val members: List<Membership> = emptyList(),
    val roles: List<Role> = emptyList(),
    val canManageInvite: Boolean = false,
    val canRemoveMembers: Boolean = false,
    val canManageRoles: Boolean = false,
    val inviteCode: String? = null,
    val isInviteSheetVisible: Boolean = false,
    val isLoading: Boolean = true,
    val menuForUserId: String? = null,
    val reassignTargetUserId: String? = null
)
