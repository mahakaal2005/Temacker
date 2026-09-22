package com.example.temacker.feature_project.presentation.roster

sealed interface RosterEvent {
    data object NavigateToManageRoles : RosterEvent
    data class NavigateToRoleExplainer(val userId: String) : RosterEvent
}
