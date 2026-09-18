package com.example.temacker.core.domain.model

// Trimmed, feature-agnostic view of feature_project's Membership+Role — NOT RolePermissions, since
// core can never import a feature's domain (architecture §8).
data class ProjectMember(
    val uid: String,
    val displayName: String,
    val photoUrl: String?,
    val roleName: String,
    val canAssignTasks: Boolean,
    val canEditAnyTask: Boolean
)
