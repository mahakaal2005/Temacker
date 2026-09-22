package com.example.temacker.feature_project.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.temacker.feature_project.presentation.create_project.CreateProjectRoot
import com.example.temacker.feature_project.presentation.gate.ProjectGateRoot
import com.example.temacker.feature_project.presentation.invited_first_run.InvitedFirstRunRoot
import com.example.temacker.feature_project.presentation.join_project.JoinProjectRoot
import com.example.temacker.feature_project.presentation.manage_roles.ManageRolesRoot
import com.example.temacker.feature_project.presentation.no_project.NoProjectRoot
import com.example.temacker.feature_project.presentation.role_explainer.RoleExplainerRoot
import com.example.temacker.feature_project.presentation.succession.SuccessionRoot
import com.example.temacker.feature_project.presentation.switch_project.SwitchProjectRoot
import com.example.temacker.feature_project.presentation.team.TeamRoot

// onNavigateToBoard/onNavigateToProfile are callbacks so feature_project never imports
// feature_tasks/feature_profile (architecture §4). Board is Phase 2's first bottom-nav tab,
// replacing Home — see specs/office/phase-2-board-baton.md.
fun NavGraphBuilder.projectGraph(
    navController: NavController,
    onNavigateToBoard: () -> Unit,
    onNavigateToInbox: () -> Unit,
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
            onNavigateToFirstRun = {
                // popUpTo removes Join so Back never returns to the code screen.
                navController.navigate(InvitedFirstRunRoute) { popUpTo(JoinProjectRoute) { inclusive = true } }
            }
        )
    }
    composable<InvitedFirstRunRoute> {
        InvitedFirstRunRoot(onNavigateToBoard = onNavigateToBoard)
    }
    composable<TeamRoute> {
        TeamRoot(
            onNavigateToBoard = onNavigateToBoard,
            onNavigateToInbox = onNavigateToInbox,
            onNavigateToYou = onNavigateToProfile,
            onNavigateToManageRoles = { navController.navigate(ManageRolesRoute) },
            onNavigateToRoleExplainer = { userId -> navController.navigate(RoleExplainerRoute(userId)) },
            onNavigateToSuccession = { navController.navigate(SuccessionRoute) },
            onNavigateToSwitchProject = { navController.navigate(SwitchProjectRoute) }
        )
    }
    composable<SwitchProjectRoute> {
        SwitchProjectRoot(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToJoinProject = { navController.navigate(JoinProjectRoute) }
        )
    }
    composable<RoleExplainerRoute> { backStackEntry ->
        val route: RoleExplainerRoute = backStackEntry.toRoute()
        RoleExplainerRoot(userId = route.userId, onNavigateBack = { navController.popBackStack() })
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
