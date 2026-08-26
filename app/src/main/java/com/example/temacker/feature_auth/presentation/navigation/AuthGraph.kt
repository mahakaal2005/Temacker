package com.example.temacker.feature_auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.temacker.feature_auth.presentation.login.LoginRoot
import com.example.temacker.feature_auth.presentation.splash.SplashRoot

// onNavigateToApp is a callback so feature_auth never imports feature_project (architecture §4).
fun NavGraphBuilder.authGraph(
    navController: NavController,
    onNavigateToApp: () -> Unit
) {
    composable<SplashRoute> {
        SplashRoot(
            onNavigateToLogin = {
                navController.navigate(LoginRoute) {
                    popUpTo(SplashRoute) { inclusive = true }
                }
            },
            onNavigateToApp = onNavigateToApp
        )
    }
    composable<LoginRoute> {
        LoginRoot(onNavigateToApp = onNavigateToApp)
    }
}
