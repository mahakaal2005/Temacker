package com.example.temacker.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Ink500

// The concept HTML's app-bar `.switcher` pill (project name + meta), now backed by real data.
// Every screen using AppScaffold's header slot renders this the same way — see architecture §8's
// seventh contract (SelectedProjectStore) and Phase 5b spec.
@Composable
fun SwitcherPill(
    projectName: String,
    metaLine: String,
    hasOtherProjects: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        // Always tappable — even a single-project user needs a way in to join a second one.
        // The chevron is just the "there's a list" affordance, shown only once there's a list.
        val rowModifier = Modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "Switch project", onClick = onClick)
        Row(
            modifier = modifier.then(rowModifier).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwitcherText(projectName, metaLine)
            if (hasOtherProjects) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Ink500)
            }
        }
    }
}

@Composable
private fun RowScope.SwitcherText(projectName: String, metaLine: String) {
    Column(modifier = Modifier.weight(1f)) {
        Text(projectName, style = MaterialTheme.typography.titleMedium)
        Text(metaLine, style = MaterialTheme.typography.labelSmall, color = Ink500)
    }
}
