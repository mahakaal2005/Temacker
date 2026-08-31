package com.example.temacker.feature_project.domain.model

data class Role(
    val id: String,
    val projectId: String,
    val name: String,
    val permissions: RolePermissions,
    val isLeader: Boolean = false
)
