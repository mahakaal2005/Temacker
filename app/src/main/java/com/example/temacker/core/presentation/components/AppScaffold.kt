package com.example.temacker.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Navy900
import com.example.temacker.core.presentation.designsystem.Paper

// Shared by Board/Inbox/Team/You — Phase 3 adds Inbox as the 4th destination.
enum class AppDestination(val label: String, val glyph: String) {
    BOARD("Board", "B"),
    INBOX("Inbox", "I"),
    TEAM("Team", "T"),
    YOU("You", "Y")
}

// Provided once by the app shell so every bottom-nav screen shows the same Inbox badge.
val LocalInboxBadgeCount = compositionLocalOf { 0 }

@Composable
fun AppScaffold(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val inboxBadge = LocalInboxBadgeCount.current
    Scaffold(
        snackbarHost = snackbarHost,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == selected,
                        onClick = { onSelect(destination) },
                        icon = {
                            // Same fixed-width slot for every glyph keeps the selected pills equal and lets the badge sit beside the "I".
                            val glyph = @Composable {
                                Box(modifier = Modifier.width(24.dp), contentAlignment = Alignment.Center) {
                                    Text(destination.glyph, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (destination == AppDestination.INBOX && inboxBadge > 0) {
                                BadgedBox(badge = { InboxBadge(inboxBadge) }) { glyph() }
                            } else {
                                glyph()
                            }
                        },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedIconColor = Ink500,
                            unselectedTextColor = Ink500
                        )
                    )
                }
            }
        }
    ) { padding -> content(padding) }
}

// Paper ring (per the mock's box-shadow) keeps the amber badge readable on the amber selected pill.
@Composable
private fun InboxBadge(count: Int) {
    Box(modifier = Modifier.offset(x = 8.dp).background(Paper, CircleShape).padding(2.dp)) {
        Badge(containerColor = Amber, contentColor = Navy900) { Text("$count") }
    }
}
