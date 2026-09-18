package com.example.temacker.feature_project.domain.model

// Bundles everything succeedProject() writes remotely so the Room-side upsert can be one @Transaction.
data class SuccessionResult(
    val newProject: Project,
    val newRoles: List<Role>,
    val newMemberships: List<Membership>
)
