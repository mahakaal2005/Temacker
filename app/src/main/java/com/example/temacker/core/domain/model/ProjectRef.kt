package com.example.temacker.core.domain.model

// Feature-agnostic id + name of one of the user's projects, for cross-project views (Inbox, badge).
data class ProjectRef(
    val id: String,
    val name: String
)
