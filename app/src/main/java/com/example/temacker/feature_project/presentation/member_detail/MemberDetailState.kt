package com.example.temacker.feature_project.presentation.member_detail

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Role

enum class MemberConfirm { NONE, REMOVE, MAKE_LEADER }

@Stable
data class MemberDetailState(
    val member: Membership? = null,
    val roles: List<Role> = emptyList(),
    val canManageRoles: Boolean = false,
    val canRemove: Boolean = false,
    val viewerIsLeader: Boolean = false,
    val isSelf: Boolean = false,
    val isRoleExpanded: Boolean = false,
    val isRolePickerVisible: Boolean = false,
    val confirm: MemberConfirm = MemberConfirm.NONE,
    val isLoading: Boolean = true,
    val error: UiText? = null
) {
    // Editing is for people with Change roles, never on the Leader and never on your own page.
    val canEdit: Boolean get() = canManageRoles && !isSelf && member?.isLeader == false
}
