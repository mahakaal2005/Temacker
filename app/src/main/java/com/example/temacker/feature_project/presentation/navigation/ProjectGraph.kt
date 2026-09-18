package com.example.temacker.feature_project.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.temacker.feature_project.presentation.create_project.CreateProjectRoot
import com.example.temacker.feature_project.presentation.gate.ProjectGateRoot
import com.example.temacker.feature_project.presentation.join_project.JoinProjectRoot
import com.example.temacker.feature_project.presentation.manage_roles.ManageRolesRoot
import com.example.temacker.feature_project.presentation.no_project.NoProjectRoot
import com.example.temacker.feature_project.presentation.succession.SuccessionRoot
import com.example.temacker.feature_project.presentation.team.TeamRoot

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
    composable<TeamRoute> {
        TeamRoot(
            onNavigateToBoard = onNavigateToBoard,
            onNavigateToYou = onNavigateToProfile,
            onNavigateToManageRoles = { navController.navigate(ManageRolesRoute) },
            onNavigateToSuccession = { navController.navigate(SuccessionRoute) }
        )
    }
    composable<ManageRolesRoute> {
        ManageRolesRoot(onNavigateBack = { navController.popBackStack() })
    }
    composable<SuccessionRoute> {
        SuccessionRoot(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToTeam = { navController.popBackStack() }
        )
    }
}
