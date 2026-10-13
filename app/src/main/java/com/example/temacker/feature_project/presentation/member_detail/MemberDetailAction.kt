package com.example.temacker.feature_project.presentation.member_detail

import com.example.temacker.feature_project.domain.model.RolePermissions

sealed interface MemberDetailAction {
    data object OnBackClick : MemberDetailAction
    data object OnToggleRole : MemberDetailAction
    data object OnChangeRoleClick : MemberDetailAction
    data object OnDismissRolePicker : MemberDetailAction
    data class OnRoleSelected(val roleId: String) : MemberDetailAction
    data class OnExtrasChanged(val extras: RolePermissions) : MemberDetailAction
    data object OnRemoveClick : MemberDetailAction
    data object OnMakeLeaderClick : MemberDetailAction
    data object OnDismissConfirm : MemberDetailAction
    data object OnConfirmClick : MemberDetailAction
    data object OnErrorDismissed : MemberDetailAction
}
