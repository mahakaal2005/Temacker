package com.example.temacker.core.domain.model

data class StuckHandoff(
    val handoffId: String,
    val taskId: String,
    val taskTitle: String,
    val fromDisplayName: String,
    val toDisplayName: String,
    val offeredAt: Long,
    val hoursStuck: Long
)
