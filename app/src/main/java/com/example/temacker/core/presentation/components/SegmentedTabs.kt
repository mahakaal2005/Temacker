package com.example.temacker.core.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.White
import com.example.temacker.core.presentation.designsystem.rememberAppHaptics
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import com.example.temacker.core.presentation.designsystem.spatialDefault

// Replaces the stock Material TabRow on Board/Team with the spec's pill track — a white thumb
// slides between segments instead of an underline. Same sliding-pill mechanic as TmkBottomBar.
@Composable
fun <T> SegmentedTabs(
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val reducedMotion = rememberReducedMotion()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s, vertical = Spacing.xxs)
            .background(NeutralWash, RoundedCornerShape(50))
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / items.size
        val selectedIndex = items.indexOf(selected).coerceAtLeast(0)
        val thumbOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = if (reducedMotion) snap() else spatialDefault(),
            label = "segmentedTabThumb"
        )
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .width(segmentWidth)
                .height(36.dp)
                .background(White, RoundedCornerShape(50))
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEach { item ->
                val isSelected = item == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clickable(role = Role.Tab, onClick = { if (!isSelected) { haptics.tick(); onSelect(item) } })
                        .semantics { this.selected = isSelected },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label(item),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Ink900 else Ink500
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedTabsPreview() {
    TemackerTheme {
        var selected by remember { mutableStateOf("Doing") }
        SegmentedTabs(items = listOf("To do", "Doing", "Done"), selected = selected, onSelect = { selected = it }, label = { it })
    }
}
