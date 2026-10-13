package com.example.temacker.core.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.White
import com.example.temacker.core.presentation.designsystem.rememberAppHaptics
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import com.example.temacker.core.presentation.designsystem.spatialDefault
import kotlinx.coroutines.delay

// How far a face slides to make room for its neighbour, and the share of it a release must pass.
private val SLIDE_DISTANCE = 56.dp
private const val COMMIT_FRACTION = 0.4f
private const val FLING_VELOCITY = 1200f
private const val END_RESISTANCE = 0.3f

// The concept HTML's app-bar `.switcher` pill (project name + meta), now backed by real data.
// Every screen using AppScaffold's header slot renders this the same way — see architecture §8's
// seventh contract (SelectedProjectStore) and Phase 5b spec.
//
// Phase 8: rebuilt as an actually-tappable pill — a surface, a border and an always-visible
// chevron, so a single-project user can still find their way to switch or add a project. The
// chevron used to only appear once you already had a second project, which was the root cause
// of "I can't see how to switch projects" — see specs/office/phase-8-ui-redesign.md §4.
//
// Swipe up for the next project, down for the previous one (phase-5b follow-up, 2026-10-14).
@Composable
fun SwitcherPill(
    projectName: String?,
    metaLine: String?,
    hasOtherProjects: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    previousProjectName: String? = null,
    nextProjectName: String? = null,
    onSwipeToPrevious: () -> Unit = {},
    onSwipeToNext: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.s, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SwitcherChip(
                    projectName = projectName,
                    metaLine = metaLine,
                    previousName = previousProjectName,
                    nextName = nextProjectName,
                    onClick = onClick,
                    onSwipeToPrevious = onSwipeToPrevious,
                    onSwipeToNext = onSwipeToNext
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun SwitcherChip(
    projectName: String?,
    metaLine: String?,
    previousName: String?,
    nextName: String?,
    onClick: () -> Unit,
    onSwipeToPrevious: () -> Unit,
    onSwipeToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Same white bordered card as the Switch project rows, so the header reads as the current row of that list.
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val container by animateColorAsState(if (isPressed) NeutralWash else White, label = "switcherPress")
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "switcherScale")
    val shape = RoundedCornerShape(18.dp)

    val haptics = rememberAppHaptics()
    val reducedMotion = rememberReducedMotion()
    val travel = with(LocalDensity.current) { SLIDE_DISTANCE.toPx() }
    var drag by remember { mutableFloatStateOf(0f) }
    var dragStartName by remember { mutableStateOf(projectName) }
    val currentName by rememberUpdatedState(projectName)
    // A new project arriving ends the slide, so the faces are back in place for the next swipe.
    LaunchedEffect(projectName) { drag = 0f }
    val settle: suspend (Float) -> Unit = { target ->
        if (reducedMotion) drag = target else animate(drag, target, animationSpec = spatialDefault()) { value, _ -> drag = value }
    }
    val dragState = rememberDraggableState { delta ->
        // Up needs a next project and down a previous one; with none, the pill only stretches a little.
        val raw = drag + delta
        val allowed = if (raw < 0) nextName != null else previousName != null
        drag = if (allowed) raw.coerceIn(-travel, travel) else (drag + delta * END_RESISTANCE).coerceIn(-travel / 4, travel / 4)
    }

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
            .draggable(
                state = dragState,
                orientation = Orientation.Vertical,
                onDragStarted = { dragStartName = projectName },
                onDragStopped = { velocity ->
                    val toNext = nextName != null && (drag < -travel * COMMIT_FRACTION || velocity < -FLING_VELOCITY)
                    val toPrevious = previousName != null && (drag > travel * COMMIT_FRACTION || velocity > FLING_VELOCITY)
                    when {
                        toNext -> { settle(-travel); haptics.tick(); onSwipeToNext() }
                        toPrevious -> { settle(travel); haptics.tick(); onSwipeToPrevious() }
                        else -> settle(0f)
                    }
                    // If the switch never lands, slide the current project back rather than leave a neighbour showing.
                    if (toNext || toPrevious) {
                        delay(4_000)
                        if (currentName == dragStartName) settle(0f)
                    }
                }
            )
            .semantics {
                customActions = listOfNotNull(
                    nextName?.let { CustomAccessibilityAction("Next project: $it") { onSwipeToNext(); true } },
                    previousName?.let { CustomAccessibilityAction("Previous project: $it") { onSwipeToPrevious(); true } }
                )
            }
            .padding(start = 10.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Once the new project's name arrives the slide is done, so the faces read as settled.
        val offset = { if (projectName == dragStartName) drag else 0f }
        SlotFaces(modifier = Modifier.weight(1f, fill = false)) {
            ProjectFace(projectName, metaLine, Modifier.graphicsLayer { translationY = offset() })
            if (previousName != null) ProjectFace(previousName, "Previous project", Modifier.graphicsLayer { translationY = offset() - travel })
            if (nextName != null) ProjectFace(nextName, "Next project", Modifier.graphicsLayer { translationY = offset() + travel })
        }
        Box(modifier = Modifier.padding(start = Spacing.s).size(28.dp).background(NeutralWash, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.UnfoldMore, contentDescription = null, tint = Ink500, modifier = Modifier.size(18.dp))
        }
    }
}

// Sizes to the current face only, so a longer neighbour name never widens the pill mid-drag.
@Composable
private fun SlotFaces(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val current = measurables.first().measure(constraints)
        val neighbours = measurables.drop(1).map { it.measure(Constraints(maxWidth = current.width, maxHeight = constraints.maxHeight)) }
        layout(current.width, current.height) {
            neighbours.forEach { it.place(0, 0) }
            current.place(0, 0)
        }
    }
}

@Composable
private fun ProjectFace(projectName: String?, metaLine: String?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ProjectTile(projectName)
        SwitcherText(projectName = projectName, metaLine = metaLine, modifier = Modifier.weight(1f, fill = false).padding(start = Spacing.s))
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
private fun SwitcherPillWithNeighboursPreview() {
    TemackerTheme {
        SwitcherPill(
            projectName = "Switcher Test Project",
            metaLine = "${memberCountLabel(2)} · Default",
            hasOtherProjects = true,
            onClick = {},
            previousProjectName = "Innogeeks Core",
            nextProjectName = "Hackathon 2026"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitcherPillLoadingPreview() {
    TemackerTheme { SwitcherPill(projectName = null, metaLine = null, hasOtherProjects = false, onClick = {}) }
}
