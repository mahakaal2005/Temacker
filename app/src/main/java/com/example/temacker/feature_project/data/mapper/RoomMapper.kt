package com.example.temacker.feature_project.data.mapper

import com.example.temacker.core.data.database.InviteCodeEntity
import com.example.temacker.core.data.database.MembershipEntity
import com.example.temacker.core.data.database.ProjectEntity
import com.example.temacker.core.data.database.RoleEntity
import com.example.temacker.feature_project.domain.model.InviteCode
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions

fun Project.toEntity() = ProjectEntity(id, name, ownerUid, createdAt)
fun ProjectEntity.toDomain() = Project(id, name, ownerUid, createdAt)

fun Role.toEntity() = RoleEntity(
    id = id,
    projectId = projectId,
    name = name,
    manageRoles = permissions.manageRoles,
    manageInviteCode = permissions.manageInviteCode,
    removeMembers = permissions.removeMembers,
    deleteProject = permissions.deleteProject,
    assignTasks = permissions.assignTasks,
    editAnyTask = permissions.editAnyTask,
    manageTags = permissions.manageTags,
    isLeader = isLeader
)

fun RoleEntity.toDomain() = Role(
    id = id,
    projectId = projectId,
    name = name,
    permissions = RolePermissions(manageRoles, manageInviteCode, removeMembers, deleteProject, assignTasks, editAnyTask, manageTags),
    isLeader = isLeader
)

fun Membership.toEntity() = MembershipEntity(
    projectId = projectId,
    userId = userId,
    roleId = roleId,
    roleName = roleName,
    manageRoles = permissions.manageRoles,
    manageInviteCode = permissions.manageInviteCode,
    removeMembers = permissions.removeMembers,
    deleteProject = permissions.deleteProject,
    assignTasks = permissions.assignTasks,
    editAnyTask = permissions.editAnyTask,
    manageTags = permissions.manageTags,
    displayName = displayName,
    photoUrl = photoUrl,
    joinedAt = joinedAt
)

fun MembershipEntity.toDomain() = Membership(
    projectId = projectId,
    userId = userId,
    roleId = roleId,
    roleName = roleName,
    permissions = RolePermissions(manageRoles, manageInviteCode, removeMembers, deleteProject, assignTasks, editAnyTask, manageTags),
    displayName = displayName,
    photoUrl = photoUrl,
    joinedAt = joinedAt
)

fun InviteCode.toEntity() = InviteCodeEntity(code, projectId, expiresAt, isActive)
fun InviteCodeEntity.toDomain() = InviteCode(code, projectId, expiresAt, isActive)
