package com.example.temacker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.first
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.presentation.components.LocalInboxBadgeCount
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.feature_auth.presentation.navigation.LoginRoute
import com.example.temacker.feature_auth.presentation.navigation.SplashRoute
import com.example.temacker.feature_auth.presentation.navigation.authGraph
import com.example.temacker.feature_profile.presentation.navigation.ProfileRoute
import com.example.temacker.feature_profile.presentation.navigation.profileGraph
import com.example.temacker.feature_project.presentation.navigation.CreateProjectRoute
import com.example.temacker.feature_project.presentation.navigation.JoinProjectRoute
import com.example.temacker.feature_project.presentation.navigation.NoProjectRoute
import com.example.temacker.feature_project.presentation.navigation.ProjectGateRoute
import com.example.temacker.feature_project.presentation.navigation.TeamRoute
import com.example.temacker.feature_project.presentation.navigation.projectGraph
import com.example.temacker.feature_tasks.domain.model.EXTRA_DESTINATION
import com.example.temacker.feature_tasks.domain.model.EXTRA_HANDOFF_ID
import com.example.temacker.feature_tasks.domain.model.EXTRA_POSTED_AT
import com.example.temacker.feature_tasks.domain.model.EXTRA_PROJECT_ID
import com.example.temacker.feature_tasks.domain.model.EXTRA_TASK_ID
import com.example.temacker.feature_tasks.domain.model.HandoffDestination
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.presentation.inbox.InboxBadgeViewModel
import com.example.temacker.feature_tasks.presentation.navigation.BoardRoute
import com.example.temacker.feature_tasks.presentation.navigation.NotificationRationaleRoute
import com.example.temacker.feature_tasks.presentation.notification_rationale.NotificationRationaleGateViewModel
import com.example.temacker.feature_tasks.presentation.navigation.InboxRoute
import com.example.temacker.feature_tasks.presentation.navigation.NotificationTarget
import com.example.temacker.feature_tasks.presentation.navigation.notificationTarget
import com.example.temacker.feature_tasks.presentation.navigation.tasksGraph

class MainActivity : ComponentActivity() {
    // A notification tap waiting to be navigated once the user is past sign-in and project setup.
    private var pendingRoute by mutableStateOf<NotificationTarget?>(null)
    // Nonce of the last notification intent handled, so a replayed intent is not acted on twice.
    private var handledPostedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handledPostedAt = savedInstanceState?.getLong(STATE_HANDLED_POSTED_AT) ?: 0L
        handleNotificationIntent(intent)
        setContent {
            TemackerTheme {
                TemackerApp(pendingRoute = pendingRoute, onPendingRouteHandled = { pendingRoute = null })
            }
        }
    }

    // singleTop delivers taps here when the app is already open.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_HANDLED_POSTED_AT, handledPostedAt)
    }

    private fun handleNotificationIntent(intent: Intent) {
        val postedAt = intent.getLongExtra(EXTRA_POSTED_AT, 0L)
        if (postedAt == 0L || postedAt == handledPostedAt) return
        handledPostedAt = postedAt
        intent.toNotificationTarget()?.let { pendingRoute = it }
    }

    private companion object {
        const val STATE_HANDLED_POSTED_AT = "handledPostedAt"
    }
}

private fun Intent.toNotificationTarget(): NotificationTarget? = notificationTarget(
    destination = getStringExtra(EXTRA_DESTINATION)?.let { name -> HandoffDestination.entries.firstOrNull { it.name == name } },
    projectId = getStringExtra(EXTRA_PROJECT_ID),
    taskId = getStringExtra(EXTRA_TASK_ID),
    handoffId = getStringExtra(EXTRA_HANDOFF_ID)
)

@Composable
private fun TemackerApp(
    pendingRoute: NotificationTarget?,
    onPendingRouteHandled: () -> Unit,
    badgeViewModel: InboxBadgeViewModel = koinViewModel(),
    rationaleGate: NotificationRationaleGateViewModel = koinViewModel(),
    observeCurrentProjectId: ObserveCurrentProjectIdUseCase = koinInject()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val atBoard = destination?.hasRoute<BoardRoute>() == true
    // Past sign-in and project setup, wherever the user happens to be (also true after the OS restores the task).
    val inApp = destination != null && listOf(
        SplashRoute::class, LoginRoute::class, ProjectGateRoute::class,
        NoProjectRoute::class, CreateProjectRoute::class, JoinProjectRoute::class
    ).none { destination.hasRoute(it) }
    val rationaleSeen by rationaleGate.seen.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Shown once, API 33+ only, and never to someone who already granted the permission.
    val needsRationale = rationaleSeen == false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    LaunchedEffect(pendingRoute, inApp, atBoard, needsRationale) {
        if (pendingRoute != null) {
            // Wait until signed in with a project so Back from the target never lands on Splash.
            if (!inApp) return@LaunchedEffect
            // The app only shows one project, so a tap for another stays where it is.
            val current = (observeCurrentProjectId().first() as? Result.Success)?.data
            if (current == pendingRoute.projectId) navController.navigate(pendingRoute.route)
            onPendingRouteHandled()
        } else if (atBoard && needsRationale) {
            navController.navigate(NotificationRationaleRoute)
        }
    }
    val inboxBadge by badgeViewModel.count.collectAsStateWithLifecycle()
    val toInbox = { navController.navigate(InboxRoute) { launchSingleTop = true } }
    CompositionLocalProvider(LocalInboxBadgeCount provides inboxBadge) {
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
                onNavigateToInbox = { toInbox() },
                onNavigateToProfile = { navController.navigate(ProfileRoute) { launchSingleTop = true } }
            )
            tasksGraph(
                navController = navController,
                onNavigateToTeam = { navController.navigate(TeamRoute) { launchSingleTop = true } },
                onNavigateToYou = { navController.navigate(ProfileRoute) { launchSingleTop = true } }
            )
            profileGraph(
                onNavigateToBoard = { navController.navigate(BoardRoute) { launchSingleTop = true } },
                onNavigateToInbox = { toInbox() },
                onNavigateToTeam = { navController.navigate(TeamRoute) { launchSingleTop = true } },
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
