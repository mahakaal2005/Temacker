package com.example.temacker.feature_project.domain.model

// displayName/photoUrl/roleName are denormalized (like permissions) so the roster never joins
// against feature_auth's User — see specs/ultimate_android_architecture.md Data Model.
data class Membership(
    val projectId: String,
    val userId: String,
    val roleId: String,
    val roleName: String,
    val permissions: RolePermissions,
    val displayName: String,
    val photoUrl: String?,
    val joinedAt: Long
)
