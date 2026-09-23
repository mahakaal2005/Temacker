package com.example.temacker.core.domain.model

// Trimmed, feature-agnostic view of the current project for the app-bar switcher pill — NOT the
// full feature_project Project (architecture §8's dependency rule: core can never import a
// feature's domain). hasOtherProjects drives the chevron; the pill itself is always tappable.
data class ProjectSummary(
    val name: String,
    val memberCount: Int,
    val roleName: String,
    val hasOtherProjects: Boolean
)
