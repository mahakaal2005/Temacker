package com.example.temacker.feature_project.data.mapper

import com.example.temacker.feature_project.domain.model.RolePermissions

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toRolePermissions(): RolePermissions = RolePermissions(
    manageRoles = this["manageRoles"] as? Boolean ?: false,
    manageInviteCode = this["manageInviteCode"] as? Boolean ?: false,
    removeMembers = this["removeMembers"] as? Boolean ?: false,
    deleteProject = this["deleteProject"] as? Boolean ?: false,
    assignTasks = this["assignTasks"] as? Boolean ?: false,
    editAnyTask = this["editAnyTask"] as? Boolean ?: false,
    manageTags = this["manageTags"] as? Boolean ?: false
)

fun RolePermissions.toFirestoreMap(): Map<String, Any?> = mapOf(
    "manageRoles" to manageRoles,
    "manageInviteCode" to manageInviteCode,
    "removeMembers" to removeMembers,
    "deleteProject" to deleteProject,
    "assignTasks" to assignTasks,
    "editAnyTask" to editAnyTask,
    "manageTags" to manageTags
)
