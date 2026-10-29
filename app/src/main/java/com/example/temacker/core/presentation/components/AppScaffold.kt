package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// The floating bar is drawn once by MainActivity above the NavHost so it stays put while screens cross-fade; selected/onSelect are kept only so call sites and previews stay unchanged.
@Composable
fun AppScaffold(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
    snackbarHost: @Composable () -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        // Scaffold only reserves snackbar space above its own bottomBar slot, and the floating
        // bar isn't in that slot — pad the host by hand so the snackbar clears it too.
        snackbarHost = { Box(modifier = Modifier.padding(bottom = TmkBottomBarReservedHeight)) { snackbarHost() } },
        topBar = { header?.invoke() },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        // The bar floats over content rather than reserving its own Scaffold slot, so content
        // draws full-bleed behind it and each screen pads for TmkBottomBarReservedHeight itself.
        Box(modifier = Modifier.fillMaxSize()) {
            content(padding)
        }
    }
}
