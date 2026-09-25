package com.example.temacker.feature_profile.domain.model

import com.example.temacker.core.domain.model.ProjectExportData
import com.example.temacker.core.domain.model.ProjectMember

// Combines feature_tasks' export data with feature_project's member list for the current project —
// feature_profile's own composite view, same exception ProfileViewModel already takes.
data class ProjectExportSnapshot(
    val projectName: String,
    val export: ProjectExportData,
    val members: List<ProjectMember>
) {
    val taskCount get() = export.tasks.size
    val handoffCount get() = export.handoffs.size
    val memberCount get() = members.size
}
