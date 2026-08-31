package com.example.temacker.feature_project.data.mapper

import com.example.temacker.feature_project.domain.model.Role
import com.google.firebase.firestore.DocumentSnapshot

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toRole(projectId: String): Role? {
    val name = getString("name") ?: return null
    val permissions = (get("permissions") as? Map<String, Any?> ?: emptyMap()).toRolePermissions()
    val isLeader = getBoolean("isLeader") ?: false
    return Role(id = id, projectId = projectId, name = name, permissions = permissions, isLeader = isLeader)
}

fun Role.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "permissions" to permissions.toFirestoreMap(),
    "isLeader" to isLeader
)
