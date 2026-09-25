package com.example.temacker.core.domain.model

// Trimmed, feature-agnostic view of feature_tasks' Task+Handoff for Phase 6 export — NOT the real
// Task/Handoff types, since core can never import a feature's domain (architecture §8), same
// trimming ProjectSummary already does for Project.
data class ExportTask(
    val id: String,
    val title: String,
    val status: String,
    val holderDisplayName: String,
    val createdByDisplayName: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class ExportHandoff(
    val id: String,
    val taskTitle: String,
    val fromDisplayName: String,
    val toDisplayName: String,
    val note: String?,
    val status: String,
    val declineReason: String?,
    val offeredAt: Long,
    val respondedAt: Long?
)

data class ProjectExportData(
    val tasks: List<ExportTask>,
    val handoffs: List<ExportHandoff>
)
