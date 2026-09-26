package com.example.temacker

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.temacker.feature_auth.presentation.navigation.LoginRoute
import com.example.temacker.feature_auth.presentation.navigation.SplashRoute
import com.example.temacker.feature_profile.presentation.navigation.ProfileRoute
import com.example.temacker.feature_project.presentation.navigation.ProjectGateRoute
import com.example.temacker.feature_project.presentation.navigation.TeamRoute
import com.example.temacker.feature_tasks.presentation.navigation.BoardRoute
import com.example.temacker.feature_tasks.presentation.navigation.InboxRoute

// Two motions: tabs slide side by side in tab order; child pages stack over their parent from the
// right and unstack to the right on Back, while the parent stays still and dims underneath.
private const val DURATION_MS = 300
private const val DIMMED_ALPHA = 0.6f

private fun NavDestination.tabIndex(): Int? = when {
    hasRoute<BoardRoute>() -> 0
    hasRoute<InboxRoute>() -> 1
    hasRoute<TeamRoute>() -> 2
    hasRoute<ProfileRoute>() -> 3
    else -> null
}

// Splash, sign-in and the project gate just cross-fade; they aren't a place you go "into".
private fun NavDestination.isEntryFlow() = hasRoute<SplashRoute>() || hasRoute<LoginRoute>() || hasRoute<ProjectGateRoute>()

private val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(DURATION_MS, easing = FastOutSlowInEasing)
private val fadeSpec = tween<Float>(DURATION_MS, easing = FastOutSlowInEasing)

private typealias Scope = AnimatedContentTransitionScope<NavBackStackEntry>

private fun Scope.tabDirection(): SlideDirection? {
    val from = initialState.destination.tabIndex() ?: return null
    val to = targetState.destination.tabIndex() ?: return null
    return if (to > from) SlideDirection.Start else SlideDirection.End
}

private fun Scope.isEntryFlow() = initialState.destination.isEntryFlow() || targetState.destination.isEntryFlow()

fun appEnter(reduced: Boolean): Scope.() -> EnterTransition = {
    val tab = tabDirection()
    when {
        reduced -> EnterTransition.None
        tab != null -> slideIntoContainer(tab, slideSpec)
        isEntryFlow() || targetState.destination.tabIndex() != null -> fadeIn(fadeSpec)
        else -> slideIntoContainer(SlideDirection.Start, slideSpec)
    }
}

fun appExit(reduced: Boolean): Scope.() -> ExitTransition = {
    val tab = tabDirection()
    when {
        reduced -> ExitTransition.None
        tab != null -> slideOutOfContainer(tab, slideSpec)
        isEntryFlow() || targetState.destination.tabIndex() != null -> ExitTransition.None
        else -> fadeOut(fadeSpec, targetAlpha = DIMMED_ALPHA)
    }
}

fun appPopEnter(reduced: Boolean): Scope.() -> EnterTransition = {
    val tab = tabDirection()
    when {
        reduced -> EnterTransition.None
        tab != null -> slideIntoContainer(tab, slideSpec)
        isEntryFlow() -> fadeIn(fadeSpec)
        else -> fadeIn(fadeSpec, initialAlpha = DIMMED_ALPHA)
    }
}

fun appPopExit(reduced: Boolean): Scope.() -> ExitTransition = {
    val tab = tabDirection()
    when {
        reduced -> ExitTransition.None
        tab != null -> slideOutOfContainer(tab, slideSpec)
        isEntryFlow() -> ExitTransition.None
        else -> slideOutOfContainer(SlideDirection.End, slideSpec)
    }
}
