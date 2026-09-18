package com.example.temacker.feature_project.data.mapper

import com.example.temacker.feature_project.domain.model.Membership
import com.google.firebase.firestore.DocumentSnapshot

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toMembership(projectId: String): Membership? {
    val userId = getString("userId") ?: return null
    val roleId = getString("roleId") ?: return null
    val roleName = getString("roleName") ?: return null
    val permissions = (get("permissions") as? Map<String, Any?> ?: emptyMap()).toRolePermissions()
    val displayName = getString("displayName") ?: return null
    val photoUrl = getString("photoUrl")
    val joinedAt = getLong("joinedAt") ?: 0L
    val isLeader = getBoolean("isLeader") ?: false
    return Membership(
        projectId = projectId,
        userId = userId,
        roleId = roleId,
        roleName = roleName,
        permissions = permissions,
        displayName = displayName,
        photoUrl = photoUrl,
        joinedAt = joinedAt,
        isLeader = isLeader
    )
}

fun Membership.toFirestoreMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "roleId" to roleId,
    "roleName" to roleName,
    "permissions" to permissions.toFirestoreMap(),
    "displayName" to displayName,
    "photoUrl" to photoUrl,
    "joinedAt" to joinedAt,
    "isLeader" to isLeader
)
