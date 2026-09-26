package com.example.temacker.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
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
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing

// The concept HTML's app-bar `.switcher` pill (project name + meta), now backed by real data.
// Every screen using AppScaffold's header slot renders this the same way — see architecture §8's
// seventh contract (SelectedProjectStore) and Phase 5b spec.
//
// Phase 8: rebuilt as an actually-tappable pill — a surface, a border and an always-visible
// chevron, so a single-project user can still find their way to switch or add a project. The
// chevron used to only appear once you already had a second project, which was the root cause
// of "I can't see how to switch projects" — see specs/office/phase-8-ui-redesign.md §4.
@Composable
fun SwitcherPill(
    projectName: String?,
    metaLine: String?,
    hasOtherProjects: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwitcherChip(projectName = projectName, metaLine = metaLine, onClick = onClick, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
    }
}

@Composable
private fun SwitcherChip(projectName: String?, metaLine: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Line),
        // Always enabled, even while isLoading — a slow or failed load must never trap someone
        // who just needs to switch away from it. isLoading only decides the skeleton visual.
        modifier = modifier.clickable(
            role = Role.Button,
            onClickLabel = "Switch or add a project",
            onClick = onClick
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProjectTile(projectName)
            SwitcherText(projectName = projectName, metaLine = metaLine, modifier = Modifier.weight(1f, fill = false).padding(start = Spacing.s))
            // Always visible — even a single-project user (or one stuck on a slow load) needs a
            // way in to join, create, or switch to a second project.
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = Ink500,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }
    }
}

@Composable
private fun ProjectTile(projectName: String?) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                if (projectName == null) NeutralWash else MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (projectName != null) {
            Text(
                projectName.take(1).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SwitcherText(projectName: String?, metaLine: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (projectName != null) {
            Text(projectName, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            metaLine?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Ink500, maxLines = 1) }
        } else {
            // Skeleton — the header never disappears while the project is still loading.
            Box(modifier = Modifier.width(90.dp).height(14.dp).background(NeutralWash, RoundedCornerShape(4.dp)))
        }
    }
}
