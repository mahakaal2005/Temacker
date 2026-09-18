package com.example.temacker.feature_project.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.temacker.feature_project.presentation.create_project.CreateProjectRoot
import com.example.temacker.feature_project.presentation.gate.ProjectGateRoot
import com.example.temacker.feature_project.presentation.join_project.JoinProjectRoot
import com.example.temacker.feature_project.presentation.manage_roles.ManageRolesRoot
import com.example.temacker.feature_project.presentation.no_project.NoProjectRoot
import com.example.temacker.feature_project.presentation.roster.RosterRoot

// onNavigateToBoard/onNavigateToProfile are callbacks so feature_project never imports
// feature_tasks/feature_profile (architecture §4). Board is Phase 2's first bottom-nav tab,
// replacing Home — see specs/office/phase-2-board-baton.md.
fun NavGraphBuilder.projectGraph(
    navController: NavController,
    onNavigateToBoard: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    composable<ProjectGateRoute> {
        ProjectGateRoot(
            onNavigateToHome = onNavigateToBoard,
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
            onNavigateToHome = onNavigateToBoard
        )
    }
    composable<JoinProjectRoute> {
        JoinProjectRoot(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHome = onNavigateToBoard
        )
    }
    composable<RosterRoute> {
        RosterRoot(
            onNavigateToBoard = onNavigateToBoard,
            onNavigateToTeam = {},
            onNavigateToYou = onNavigateToProfile,
            onNavigateToManageRoles = { navController.navigate(ManageRolesRoute) }
        )
    }
    composable<ManageRolesRoute> {
        ManageRolesRoot(onNavigateBack = { navController.popBackStack() })
    }
}
