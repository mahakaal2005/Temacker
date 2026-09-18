package com.example.temacker.feature_profile.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.temacker.feature_profile.presentation.profile.ProfileRoot

// onNavigateToBoard/onNavigateToTeam/onNavigateToLogin are callbacks so feature_profile never
// imports feature_tasks/feature_project/feature_auth (architecture §4).
fun NavGraphBuilder.profileGraph(
    onNavigateToBoard: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    composable<ProfileRoute> {
        ProfileRoot(
            onNavigateToBoard = onNavigateToBoard,
            onNavigateToTeam = onNavigateToTeam,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}
