package com.example.temacker.feature_project.presentation.roster

sealed interface RosterAction {
    data object OnInviteClick : RosterAction
    data object OnDismissInviteSheet : RosterAction
    data object OnGenerateNewCodeClick : RosterAction
    data object OnCopyCodeClick : RosterAction
    data class OnMemberMoreClick(val userId: String) : RosterAction
    data object OnDismissMemberMenu : RosterAction
    data class OnRemoveMemberClick(val userId: String) : RosterAction
    data class OnReassignRoleClick(val userId: String) : RosterAction
    data object OnDismissReassignSheet : RosterAction
    data class OnRoleSelected(val roleId: String) : RosterAction
    data object OnManageRolesClick : RosterAction
    data object OnErrorDismissed : RosterAction
}
