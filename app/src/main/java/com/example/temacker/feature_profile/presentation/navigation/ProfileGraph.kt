package com.example.temacker.feature_profile.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.temacker.feature_profile.presentation.plan_limits.PlanLimitsRoot
import com.example.temacker.feature_profile.presentation.profile.ProfileRoot
import com.example.temacker.feature_profile.presentation.your_data.YourDataRoot

// onNavigateToBoard/onNavigateToTeam/onNavigateToLogin are callbacks so feature_profile never
// imports feature_tasks/feature_project/feature_auth (architecture §4). Your Data/Plan & Limits
// (Phase 6) are routes within this same graph, so their own back navigation uses navController
// directly, same as projectGraph/tasksGraph's internal routes.
fun NavGraphBuilder.profileGraph(
    navController: NavController,
    onNavigateToBoard: () -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProjectGate: () -> Unit,
    onNavigateToSwitchProject: () -> Unit,
    onNavigateToYourData: () -> Unit,
    onNavigateToPlanLimits: () -> Unit
) {
    composable<ProfileRoute> {
        ProfileRoot(
            onNavigateToBoard = onNavigateToBoard,
            onNavigateToInbox = onNavigateToInbox,
            onNavigateToTeam = onNavigateToTeam,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToProjectGate = onNavigateToProjectGate,
            onNavigateToSwitchProject = onNavigateToSwitchProject,
            onNavigateToYourData = onNavigateToYourData,
            onNavigateToPlanLimits = onNavigateToPlanLimits
        )
    }
    composable<YourDataRoute> {
        YourDataRoot(
            onNavigateBack = { navController.popBackStack() },
            // Deleting the account leaves every project, so the next screen is Login, not Back.
            onAccountDeleted = onNavigateToLogin
        )
    }
    composable<PlanLimitsRoute> {
        PlanLimitsRoot(onNavigateBack = { navController.popBackStack() })
    }
}
