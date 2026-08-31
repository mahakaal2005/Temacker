package com.example.temacker.feature_project.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.temacker.feature_project.presentation.create_project.CreateProjectRoot
import com.example.temacker.feature_project.presentation.gate.ProjectGateRoot
import com.example.temacker.feature_project.presentation.home.HomeRoot
import com.example.temacker.feature_project.presentation.join_project.JoinProjectRoot
import com.example.temacker.feature_project.presentation.manage_roles.ManageRolesRoot
import com.example.temacker.feature_project.presentation.no_project.NoProjectRoot
import com.example.temacker.feature_project.presentation.roster.RosterRoot

// onNavigateToProfile is a callback so feature_project never imports feature_profile (architecture §4).
fun NavGraphBuilder.projectGraph(
    navController: NavController,
    onNavigateToProfile: () -> Unit
) {
    composable<ProjectGateRoute> {
        ProjectGateRoot(
            onNavigateToHome = {
                navController.navigate(HomeRoute) { popUpTo(ProjectGateRoute) { inclusive = true } }
            },
            onNavigateToNoProject = {
                navController.navigate(NoProjectRoute) { popUpTo(ProjectGateRoute) { inclusive = true } }
            }
        )
    }
    composable<NoProjectRoute> {
        NoProjectRoot(
            onNavigateToCreateProject = { navController.navigate(CreateProjectRoute) },
            onNavigateToJoinProject = { navController.navigate(JoinProjectRoute) },
            onNavigateToProfile = onNavigateToProfile
        )
    }
    composable<CreateProjectRoute> {
        CreateProjectRoot(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHome = {
                navController.navigate(HomeRoute) { popUpTo(NoProjectRoute) { inclusive = true } }
            }
        )
    }
    composable<JoinProjectRoute> {
        JoinProjectRoot(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHome = {
                navController.navigate(HomeRoute) { popUpTo(NoProjectRoute) { inclusive = true } }
            }
        )
    }
    composable<HomeRoute> {
        HomeRoot(
            onNavigateToHome = {},
            onNavigateToRoster = { navController.navigate(RosterRoute) { launchSingleTop = true } },
            onNavigateToProfile = onNavigateToProfile
        )
    }
    composable<RosterRoute> {
        RosterRoot(
            onNavigateToHome = { navController.navigate(HomeRoute) { launchSingleTop = true } },
            onNavigateToRoster = {},
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToManageRoles = { navController.navigate(ManageRolesRoute) }
        )
    }
    composable<ManageRolesRoute> {
        ManageRolesRoot(onNavigateBack = { navController.popBackStack() })
    }
}
