package com.example.temacker.feature_project.domain.model

data class RolePermissions(
    val manageRoles: Boolean = false,
    val manageInviteCode: Boolean = false,
    val removeMembers: Boolean = false,
    val deleteProject: Boolean = false,
    val assignTasks: Boolean = false,
    val editAnyTask: Boolean = false,
    val manageTags: Boolean = false
) {
    companion object {
        val ALL_GRANTED = RolePermissions(true, true, true, true, true, true, true)
        val NONE = RolePermissions()
    }
}
