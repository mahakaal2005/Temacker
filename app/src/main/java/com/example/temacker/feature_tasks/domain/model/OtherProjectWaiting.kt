package com.example.temacker.feature_tasks.domain.model

// Handoffs waiting on the current user in a project other than the one currently selected.
data class OtherProjectWaiting(
    val projectId: String,
    val projectName: String,
    val entries: List<InboxEntry>
)
