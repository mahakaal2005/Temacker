package com.example.temacker.core.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.effectsSpring
import com.example.temacker.core.presentation.designsystem.spatialDefault

// One full-width, fully-tappable strip for Board's sync/waiting/other-projects banners (they used
// to be 3 near-identical hand-rolled copies, and only the text column, not the whole row, took
// taps). Enters/exits with AnimatedVisibility per phase-8-ui-redesign.md §3.
@Composable
fun InfoStrip(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    background: Color = MaterialTheme.colorScheme.background,
    leading: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = spatialDefault()) + fadeIn(animationSpec = effectsSpring()),
        exit = shrinkVertically(animationSpec = spatialDefault()) + fadeOut(animationSpec = effectsSpring())
    ) {
        Surface(
            color = background,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.s, vertical = Spacing.xxs)
                .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                leading?.invoke()
                Column {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text(detail, style = MaterialTheme.typography.bodySmall, color = Ink500)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoStripPreview() {
    TemackerTheme {
        InfoStrip(
            title = "2 handoffs waiting on you",
            detail = "Tap to see only these",
            background = AmberWash,
            onClick = {}
        )
    }
}
