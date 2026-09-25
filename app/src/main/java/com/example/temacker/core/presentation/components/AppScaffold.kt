package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// AppDestination/LocalInboxBadgeCount live in TmkBottomBar.kt alongside the bar that renders them.
@Composable
fun AppScaffold(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
    snackbarHost: @Composable () -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = snackbarHost,
        topBar = { header?.invoke() },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        // The bar floats over content rather than reserving its own Scaffold slot, so content
        // draws full-bleed behind it and each screen pads for TmkBottomBarReservedHeight itself.
        Box(modifier = Modifier.fillMaxSize()) {
            content(padding)
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                TmkBottomBar(selected = selected, onSelect = onSelect)
            }
        }
    }
}
