package com.example.temacker.feature_project.data.mapper

import com.example.temacker.feature_project.domain.model.InviteCode
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toInviteCode(): InviteCode? {
    val projectId = getString("projectId") ?: return null
    val expiresAt = getLong("expiresAt")
    val isActive = getBoolean("isActive") ?: false
    return InviteCode(code = id, projectId = projectId, expiresAt = expiresAt, isActive = isActive)
}

fun InviteCode.toFirestoreMap(): Map<String, Any?> = mapOf(
    "projectId" to projectId,
    "expiresAt" to expiresAt,
    "isActive" to isActive
)
