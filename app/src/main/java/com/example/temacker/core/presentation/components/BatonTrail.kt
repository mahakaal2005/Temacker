package com.example.temacker.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.Coral
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.LineSoft
import com.example.temacker.core.presentation.designsystem.Paper
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.Teal
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import com.example.temacker.core.presentation.designsystem.spatialDefault
import com.example.temacker.core.presentation.designsystem.spatialExpressive

// Task detail's "signature component" per the spec: a connector line runs behind every node.
// Amber = created, teal = accepted, hollow = still open (offered, unanswered), coral outline =
// declined. The newest node pulses once on entry (skipped under reduced motion).
enum class TrailNodeKind { CREATED, ACCEPTED, OFFERED_WAITING, DECLINED }

data class TrailNode(val headline: String, val detail: String?, val note: String?, val kind: TrailNodeKind)

@Composable
fun BatonTrail(nodes: List<TrailNode>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        nodes.forEachIndexed { index, node ->
            TrailRow(node = node, isFirst = index == 0, isLast = index == nodes.lastIndex)
        }
    }
}

@Composable
private fun TrailRow(node: TrailNode, isFirst: Boolean, isLast: Boolean) {
    val reducedMotion = rememberReducedMotion()
    val pulseScale = remember { Animatable(1f) }
    // Keyed on the node itself too — not just isFirst/reducedMotion — so a genuinely new newest
    // node re-triggers the pulse instead of silently reusing a prior effect for the same slot.
    LaunchedEffect(node, isFirst, reducedMotion) {
        if (isFirst && !reducedMotion) {
            pulseScale.animateTo(1.4f, animationSpec = spatialExpressive())
            pulseScale.animateTo(1f, animationSpec = spatialDefault())
        }
    }
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        TrailConnector(kind = node.kind, isFirst = isFirst, isLast = isLast, pulseScale = { pulseScale.value })
        Column(modifier = Modifier.padding(start = Spacing.s, bottom = Spacing.m)) {
            Text(node.headline, style = MaterialTheme.typography.bodyMedium)
            node.detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Ink500, modifier = Modifier.padding(top = 2.dp)) }
            node.note?.let { Text("“$it”", style = MaterialTheme.typography.bodySmall, color = Ink500, modifier = Modifier.padding(top = 2.dp)) }
        }
    }
}

// dotCenterY approximates the first text line's vertical center; it isn't measured against the
// adjacent Column's real layout, so nudge it if the connector looks slightly off once rendered.
private val dotCenterY = 14.dp
private val dotRadius = 5.dp
private val strokeWidth = 2.dp

@Composable
private fun TrailConnector(kind: TrailNodeKind, isFirst: Boolean, isLast: Boolean, pulseScale: () -> Float) {
    Canvas(modifier = Modifier.fillMaxHeight().width(24.dp).graphicsLayer { val s = pulseScale(); scaleX = s; scaleY = s }) {
        val centerX = size.width / 2
        val centerYPx = dotCenterY.toPx()
        val radiusPx = dotRadius.toPx()
        val strokePx = strokeWidth.toPx()
        if (!isFirst) drawLine(LineSoft, Offset(centerX, 0f), Offset(centerX, centerYPx - radiusPx), strokeWidth = strokePx)
        if (!isLast) drawLine(LineSoft, Offset(centerX, centerYPx + radiusPx), Offset(centerX, size.height), strokeWidth = strokePx)
        when (kind) {
            TrailNodeKind.CREATED -> drawCircle(Amber, radiusPx, Offset(centerX, centerYPx))
            TrailNodeKind.ACCEPTED -> drawCircle(Teal, radiusPx, Offset(centerX, centerYPx))
            TrailNodeKind.OFFERED_WAITING -> {
                drawCircle(Paper, radiusPx, Offset(centerX, centerYPx))
                drawCircle(Ink500, radiusPx, Offset(centerX, centerYPx), style = Stroke(width = strokePx))
            }
            TrailNodeKind.DECLINED -> {
                drawCircle(Paper, radiusPx, Offset(centerX, centerYPx))
                drawCircle(Coral, radiusPx, Offset(centerX, centerYPx), style = Stroke(width = strokePx))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BatonTrailPreview() {
    TemackerTheme {
        BatonTrail(
            nodes = listOf(
                TrailNode("Offered to you — waiting", "2 hours ago", "Design's done — needs a print quote before Friday.", TrailNodeKind.OFFERED_WAITING),
                TrailNode("Mei-Ling Chow accepted from Daniel Osei", "23 Aug · held 3 days", null, TrailNodeKind.ACCEPTED),
                TrailNode("Tomas Vidal declined", null, "I'm away until the 30th", TrailNodeKind.DECLINED),
                TrailNode("Daniel Osei accepted from you", "18 Aug · held 5 days", null, TrailNodeKind.ACCEPTED),
                TrailNode("You created this task", "17 Aug", null, TrailNodeKind.CREATED)
            )
        )
    }
}
