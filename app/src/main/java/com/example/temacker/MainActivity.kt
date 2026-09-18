package com.example.temacker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.feature_auth.presentation.navigation.LoginRoute
import com.example.temacker.feature_auth.presentation.navigation.SplashRoute
import com.example.temacker.feature_auth.presentation.navigation.authGraph
import com.example.temacker.feature_profile.presentation.navigation.ProfileRoute
import com.example.temacker.feature_profile.presentation.navigation.profileGraph
import com.example.temacker.feature_project.presentation.navigation.ProjectGateRoute
import com.example.temacker.feature_project.presentation.navigation.TeamRoute
import com.example.temacker.feature_project.presentation.navigation.projectGraph
import com.example.temacker.feature_tasks.presentation.navigation.BoardRoute
import com.example.temacker.feature_tasks.presentation.navigation.tasksGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemackerTheme {
                TemackerApp()
            }
        }
    }
}

@Composable
private fun TemackerApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SplashRoute) {
        authGraph(
            navController = navController,
            onNavigateToApp = {
                navController.navigate(ProjectGateRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
        projectGraph(
            navController = navController,
            onNavigateToBoard = { navController.navigate(BoardRoute) { launchSingleTop = true } },
            onNavigateToProfile = { navController.navigate(ProfileRoute) { launchSingleTop = true } }
        )
        tasksGraph(
            navController = navController,
            onNavigateToTeam = { navController.navigate(TeamRoute) { launchSingleTop = true } },
            onNavigateToYou = { navController.navigate(ProfileRoute) { launchSingleTop = true } }
        )
        profileGraph(
            onNavigateToBoard = { navController.navigate(BoardRoute) { launchSingleTop = true } },
            onNavigateToTeam = { navController.navigate(TeamRoute) { launchSingleTop = true } },
            onNavigateToLogin = {
                navController.navigate(LoginRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
