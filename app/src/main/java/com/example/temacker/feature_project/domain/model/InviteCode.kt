package com.example.temacker.feature_project.domain.model

data class InviteCode(
    val code: String,
    val projectId: String,
    val expiresAt: Long?,
    val isActive: Boolean
)
