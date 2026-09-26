package com.example.temacker.core.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion

// A centered spinner — the one pattern 24 screens each rebuilt by hand.
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

// A shimmering placeholder for a list that's still loading, shaped like its real rows rather than
// a spinner that gives no sense of what's coming. Shimmer stops under reduced motion.
@Composable
fun SkeletonList(rowCount: Int = 6, rowHeight: Dp = 64.dp, modifier: Modifier = Modifier) {
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

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    TemackerTheme { LoadingState() }
}

@Preview(showBackground = true)
@Composable
private fun SkeletonListPreview() {
    TemackerTheme { SkeletonList(rowCount = 4) }
}
