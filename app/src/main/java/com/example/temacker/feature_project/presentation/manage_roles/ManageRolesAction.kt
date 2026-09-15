package com.example.temacker.feature_project.presentation.manage_roles

import com.example.temacker.feature_project.domain.model.RolePermissions

sealed interface ManageRolesAction {
    data object OnBackClick : ManageRolesAction
    data object OnAddRoleClick : ManageRolesAction
    data object OnDismissCreateDialog : ManageRolesAction
    data class OnNewRoleNameChange(val value: String) : ManageRolesAction
    data object OnCreateRoleConfirm : ManageRolesAction
    data class OnPermissionToggle(val roleId: String, val permissions: RolePermissions) : ManageRolesAction
    data class OnDeleteRoleClick(val roleId: String) : ManageRolesAction
    data object OnErrorDismissed : ManageRolesAction
}
