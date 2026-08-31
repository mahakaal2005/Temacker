package com.example.temacker.feature_project.domain.model

data class Project(
    val id: String,
    val name: String,
    val ownerUid: String,
    val createdAt: Long
)
