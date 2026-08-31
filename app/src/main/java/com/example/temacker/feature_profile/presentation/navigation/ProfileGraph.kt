package com.example.temacker.feature_profile.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.temacker.feature_profile.presentation.profile.ProfileRoot

// onNavigateToHome/onNavigateToRoster/onNavigateToLogin are callbacks so feature_profile never
// imports feature_project or feature_auth (architecture §4).
fun NavGraphBuilder.profileGraph(
    onNavigateToHome: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    composable<ProfileRoute> {
        ProfileRoot(
            onNavigateToHome = onNavigateToHome,
            onNavigateToRoster = onNavigateToRoster,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}
