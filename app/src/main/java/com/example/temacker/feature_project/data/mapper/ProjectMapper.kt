package com.example.temacker.feature_project.data.mapper

import com.example.temacker.feature_project.domain.model.Project
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toProject(): Project? {
    val name = getString("name") ?: return null
    val ownerUid = getString("ownerUid") ?: return null
    val createdAt = getLong("createdAt") ?: 0L
    return Project(id = id, name = name, ownerUid = ownerUid, createdAt = createdAt)
}

fun Project.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "ownerUid" to ownerUid,
    "createdAt" to createdAt
)
