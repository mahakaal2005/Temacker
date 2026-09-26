package com.example.temacker.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.White
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme

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
            modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.s, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SwitcherChip(projectName = projectName, metaLine = metaLine, onClick = onClick)
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun SwitcherChip(projectName: String?, metaLine: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Same white bordered card as the Switch project rows, so the header reads as the current row of that list.
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val container by animateColorAsState(if (isPressed) NeutralWash else White, label = "switcherPress")
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "switcherScale")
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; transformOrigin = TransformOrigin(0f, 0.5f) }
            .clip(shape)
            .background(container)
            .border(1.dp, Line, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Switch or add a project",
                onClick = onClick
            )
            .padding(start = 10.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProjectTile(projectName)
        SwitcherText(projectName = projectName, metaLine = metaLine, modifier = Modifier.weight(1f, fill = false).padding(horizontal = Spacing.s))
        Box(modifier = Modifier.size(28.dp).background(NeutralWash, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.UnfoldMore, contentDescription = null, tint = Ink500, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ProjectTile(projectName: String?) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                if (projectName == null) NeutralWash else Amber.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (projectName != null) {
            Text(
                projectName.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AmberInk
            )
        }
    }
}

@Composable
private fun SwitcherText(projectName: String?, metaLine: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (projectName != null) {
            Text(
                projectName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            metaLine?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Ink500, maxLines = 1) }
        } else {
            // Skeleton — the header never disappears while the project is still loading.
            Box(modifier = Modifier.width(110.dp).height(18.dp).background(NeutralWash, RoundedCornerShape(4.dp)))
        }
    }
}

fun memberCountLabel(count: Int) = if (count == 1) "1 member" else "$count members"

@Preview(showBackground = true)
@Composable
private fun SwitcherPillPreview() {
    TemackerTheme {
        SwitcherPill(projectName = "pp0", metaLine = "${memberCountLabel(1)} · Leader", hasOtherProjects = false, onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitcherPillLoadingPreview() {
    TemackerTheme { SwitcherPill(projectName = null, metaLine = null, hasOtherProjects = false, onClick = {}) }
}
