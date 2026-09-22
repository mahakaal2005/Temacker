package com.example.temacker.feature_project.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable data object ProjectGateRoute
@Serializable data object NoProjectRoute
@Serializable data object CreateProjectRoute
@Serializable data object JoinProjectRoute
@Serializable data object InvitedFirstRunRoute
@Serializable data object TeamRoute
@Serializable data class RoleExplainerRoute(val userId: String)
@Serializable data object ManageRolesRoute
@Serializable data object SuccessionRoute
