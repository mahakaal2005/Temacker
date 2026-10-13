package com.example.temacker.core.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import kotlinx.coroutines.delay

// Text waits a beat so fast loads never flash it; the slow hint covers a weak connection.
private const val TEXT_DELAY_MS = 400L
private const val SLOW_AFTER_MS = 6_000L
private const val SLOW_BODY = "This is taking longer than usual. Your connection might be slow, so we'll keep trying."
private const val SLOW_CAPTION = "Still loading. Your connection might be slow."

// Full-screen loading for a single thing, laid out like EmptyState so the user always knows what's happening.
@Composable
fun LoadingState(icon: ImageVector, title: String, body: String, modifier: Modifier = Modifier) {
    LoadingStateContent(
        icon = icon,
        title = title,
        body = body,
        showText = rememberShownAfter(TEXT_DELAY_MS),
        isSlow = rememberShownAfter(SLOW_AFTER_MS),
        modifier = modifier
    )
}

// A shimmering placeholder for a list that's still loading, shaped like its real rows; the caption says what's coming.
@Composable
fun SkeletonList(rowCount: Int = 6, rowHeight: Dp = 64.dp, caption: String? = null, modifier: Modifier = Modifier) {
    SkeletonListContent(rowCount, rowHeight, caption, isSlow = rememberShownAfter(SLOW_AFTER_MS), modifier = modifier)
}

@Composable
private fun rememberShownAfter(delayMillis: Long): Boolean {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        shown = true
    }
    return shown
}

@Composable
private fun LoadingStateContent(
    icon: ImageVector,
    title: String,
    body: String,
    showText: Boolean,
    isSlow: Boolean,
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotion()
    val textAlpha by animateFloatAsState(
        targetValue = if (showText) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else tween(300),
        label = "loadingTextAlpha"
    )
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
                strokeWidth = 3.dp,
                modifier = Modifier.fillMaxSize()
            )
            Surface(shape = CircleShape, color = NeutralWash, modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = Ink500, modifier = Modifier.size(28.dp))
                }
            }
        }
        // Polite live region so TalkBack reads the status, and the slow hint when it appears.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer { alpha = textAlpha }
                .semantics { liveRegion = LiveRegionMode.Polite }
        ) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.m)
            )
            Crossfade(targetState = isSlow, animationSpec = if (reducedMotion) snap() else tween(300), label = "loadingBody") { slow ->
                Text(
                    if (slow) SLOW_BODY else body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun SkeletonListContent(rowCount: Int, rowHeight: Dp, caption: String?, isSlow: Boolean, modifier: Modifier = Modifier) {
    val reducedMotion = rememberReducedMotion()
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val alpha by transition.animateFloat(
        initialValue = if (reducedMotion) 0.6f else 0.4f,
        targetValue = if (reducedMotion) 0.6f else 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (reducedMotion) 1 else 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonShimmerAlpha"
    )
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.s)) {
        if (caption != null) LoadingCaption(text = if (isSlow) SLOW_CAPTION else caption)
        repeat(rowCount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .padding(vertical = Spacing.xxs)
                    .graphicsLayer { this.alpha = alpha }
                    .background(NeutralWash, RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
private fun LoadingCaption(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = Spacing.xxs, vertical = Spacing.xs)
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.dp,
            modifier = Modifier.size(16.dp)
        )
        Text(text, style = MaterialTheme.typography.labelLarge, color = Ink500, modifier = Modifier.padding(start = Spacing.xs))
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStateStartPreview() {
    TemackerTheme {
        LoadingStateContent(Icons.Rounded.FolderOpen, "Loading your projects", "Checking which projects you're in…", showText = false, isSlow = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    TemackerTheme {
        LoadingStateContent(Icons.Rounded.FolderOpen, "Loading your projects", "Checking which projects you're in…", showText = true, isSlow = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStateSlowPreview() {
    TemackerTheme {
        LoadingStateContent(Icons.Rounded.FolderOpen, "Loading your projects", "Checking which projects you're in…", showText = true, isSlow = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonListPreview() {
    TemackerTheme { SkeletonListContent(rowCount = 4, rowHeight = 64.dp, caption = "Loading tasks…", isSlow = false) }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonListSlowPreview() {
    TemackerTheme { SkeletonListContent(rowCount = 4, rowHeight = 64.dp, caption = "Loading tasks…", isSlow = true) }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonListNoCaptionPreview() {
    TemackerTheme { SkeletonListContent(rowCount = 4, rowHeight = 64.dp, caption = null, isSlow = false) }
}
