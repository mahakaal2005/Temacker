package com.example.temacker.core.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.Navy900
import com.example.temacker.core.presentation.designsystem.Paper
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.White
import com.example.temacker.core.presentation.designsystem.floatingElevation
import com.example.temacker.core.presentation.designsystem.rememberAppHaptics
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import com.example.temacker.core.presentation.designsystem.spatialExpressive

// Shared by Board/Inbox/Team/You — Phase 3 adds Inbox as the 4th destination.
enum class AppDestination(val label: String, val outlineIcon: ImageVector, val filledIcon: ImageVector) {
    BOARD("Board", Icons.Outlined.Dashboard, Icons.Rounded.Dashboard),
    INBOX("Inbox", Icons.Outlined.Inbox, Icons.Rounded.Inbox),
    TEAM("Team", Icons.Outlined.Groups, Icons.Rounded.Groups),
    YOU("You", Icons.Outlined.Person, Icons.Rounded.Person)
}

// Provided once by the app shell so every bottom-nav screen shows the same Inbox badge.
val LocalInboxBadgeCount = compositionLocalOf { 0 }

private val barHeight = 64.dp
private val barMargin = 12.dp

// Total space a screen must reserve at the bottom so its last item clears the floating bar.
val TmkBottomBarReservedHeight = barHeight + barMargin * 2

// Floating pill nav (Phase 8) — replaces the full-width Material NavigationBar. The amber
// selection pill slides between tabs instead of jumping, and each tab ticks once on select.
@Composable
fun TmkBottomBar(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val inboxBadge = LocalInboxBadgeCount.current
    val haptics = rememberAppHaptics()
    Surface(
        color = White,
        shape = RoundedCornerShape(32.dp),
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = barMargin, vertical = barMargin)
            .fillMaxWidth()
            .height(barHeight)
            .floatingElevation(RoundedCornerShape(32.dp))
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AppDestination.entries.forEach { destination ->
                val isSelected = destination == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            role = Role.Tab,
                            onClickLabel = destination.label,
                            onClick = {
                                if (!isSelected) {
                                    haptics.tick()
                                    onSelect(destination)
                                }
                            }
                        )
                        .semantics {
                            if (destination == AppDestination.INBOX && inboxBadge > 0) {
                                stateDescription = "$inboxBadge waiting"
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NavPill(destination = destination, isSelected = isSelected, badgeCount = if (destination == AppDestination.INBOX) inboxBadge else 0)
                    Text(
                        destination.label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Ink900 else Ink500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavPill(destination: AppDestination, isSelected: Boolean, badgeCount: Int) {
    val reducedMotion = rememberReducedMotion()
    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 0.dp,
        animationSpec = if (reducedMotion) snap() else spatialExpressive(),
        label = "navPillWidth"
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .width(pillWidth)
                .background(Amber.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        )
        Box {
            Crossfade(targetState = isSelected, label = "navIcon") { selected ->
                Icon(
                    imageVector = if (selected) destination.filledIcon else destination.outlineIcon,
                    contentDescription = null,
                    tint = if (selected) Navy900 else Ink500,
                    modifier = Modifier.size(23.dp)
                )
            }
            if (badgeCount > 0) {
                InboxBadge(count = badgeCount, modifier = Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-4).dp))
            }
        }
    }
}

// Paper ring (per the mock's box-shadow) keeps the amber badge readable on the amber selected pill.
@Composable
private fun InboxBadge(count: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Paper, CircleShape).padding(2.dp)) {
        Badge(containerColor = Amber, contentColor = Navy900) { Text("$count") }
    }
}

// Screens padding their content for the floating bar reuse this rather than guessing a number.
fun bottomBarContentPadding(padding: PaddingValues): PaddingValues = PaddingValues(
    top = padding.calculateTopPadding(),
    bottom = TmkBottomBarReservedHeight + Spacing.xs
)
