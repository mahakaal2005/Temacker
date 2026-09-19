package com.example.temacker.feature_tasks.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.temacker.feature_tasks.presentation.board.BoardRoot
import com.example.temacker.feature_tasks.presentation.decline.DeclineRoot
import com.example.temacker.feature_tasks.presentation.handoff.HandoffRoot
import com.example.temacker.feature_tasks.presentation.inbox.InboxRoot
import com.example.temacker.feature_tasks.presentation.incoming.IncomingRoot
import com.example.temacker.feature_tasks.presentation.new_task.NewTaskRoot
import com.example.temacker.feature_tasks.presentation.task_detail.TaskDetailRoot

// onNavigateToTeam/onNavigateToYou are callbacks so feature_tasks never imports
// feature_project/feature_profile (architecture §4).
fun NavGraphBuilder.tasksGraph(
    navController: NavController,
    onNavigateToTeam: () -> Unit,
    onNavigateToYou: () -> Unit
) {
    composable<BoardRoute> {
        BoardRoot(
            onNavigateToInbox = { navController.navigate(InboxRoute) { launchSingleTop = true } },
            onNavigateToTeam = onNavigateToTeam,
            onNavigateToYou = onNavigateToYou,
            onNavigateToNewTask = { navController.navigate(NewTaskRoute) },
            onNavigateToTaskDetail = { taskId -> navController.navigate(TaskDetailRoute(taskId)) },
            onNavigateToIncoming = { taskId, handoffId -> navController.navigate(IncomingRoute(taskId, handoffId)) }
        )
    }
    composable<InboxRoute> {
        InboxRoot(
            onNavigateToBoard = { navController.navigate(BoardRoute) { launchSingleTop = true } },
            onNavigateToTeam = onNavigateToTeam,
            onNavigateToYou = onNavigateToYou,
            onNavigateToIncoming = { taskId, handoffId -> navController.navigate(IncomingRoute(taskId, handoffId)) },
            onNavigateToTaskDetail = { taskId -> navController.navigate(TaskDetailRoute(taskId)) }
        )
    }
    composable<NewTaskRoute> {
        NewTaskRoot(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToBoard = { navController.popBackStack() }
        )
    }
    composable<TaskDetailRoute> { backStackEntry ->
        val route: TaskDetailRoute = backStackEntry.toRoute()
        TaskDetailRoot(
            taskId = route.taskId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHandoff = { taskId -> navController.navigate(HandoffRoute(taskId)) }
        )
    }
    composable<HandoffRoute> { backStackEntry ->
        val route: HandoffRoute = backStackEntry.toRoute()
        HandoffRoot(
            taskId = route.taskId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
    composable<IncomingRoute> { backStackEntry ->
        val route: IncomingRoute = backStackEntry.toRoute()
        IncomingRoot(
            taskId = route.taskId,
            handoffId = route.handoffId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDecline = { taskId, handoffId -> navController.navigate(DeclineRoute(taskId, handoffId)) }
        )
    }
    composable<DeclineRoute> { backStackEntry ->
        val route: DeclineRoute = backStackEntry.toRoute()
        DeclineRoot(
            taskId = route.taskId,
            handoffId = route.handoffId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToBoard = {
                navController.navigate(BoardRoute) { popUpTo(BoardRoute) { inclusive = true } }
            }
        )
    }
}
