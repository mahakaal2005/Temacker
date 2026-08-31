package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.temacker.core.presentation.designsystem.Ink500

// Shared by Home/Roster/Profile — the three Phase 1 bottom-nav destinations (Board joins in Phase 2).
enum class AppDestination(val label: String, val glyph: String) {
    HOME("Home", "H"),
    ROSTER("Roster", "R"),
    PROFILE("Profile", "P")
}

@Composable
fun AppScaffold(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = destination == selected,
                        onClick = { onSelect(destination) },
                        icon = { Text(destination.glyph, fontWeight = FontWeight.Bold) },
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
