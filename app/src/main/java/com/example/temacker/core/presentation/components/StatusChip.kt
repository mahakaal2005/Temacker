package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.CoralInk
import com.example.temacker.core.presentation.designsystem.CoralWash
import com.example.temacker.core.presentation.designsystem.Ink600
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash

// One pill for every status the app shows ("For you", "Queued", "Not sent", "2 days late", a due
// date) instead of each screen rebuilding its own Surface+Text. An optional icon means color is
// never the only signal — see phase-8-ui-redesign.md §3/§5.
enum class ChipTone { WARNING, DANGER, SUCCESS, NEUTRAL }

@Composable
fun StatusChip(text: String, tone: ChipTone, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    val (background, foreground) = tone.colors()
    Surface(color = background, shape = RoundedCornerShape(50), modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.xs, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(it, contentDescription = null, tint = foreground, modifier = Modifier.padding(end = 4.dp))
            }
            Text(text, style = MaterialTheme.typography.labelSmall, color = foreground)
        }
    }
}

private fun ChipTone.colors(): Pair<Color, Color> = when (this) {
    ChipTone.WARNING -> AmberWash to AmberInk
    ChipTone.DANGER -> CoralWash to CoralInk
    ChipTone.SUCCESS -> TealWash to TealInk
    ChipTone.NEUTRAL -> NeutralWash to Ink600
}

@Preview(showBackground = true)
@Composable
private fun StatusChipPreview() {
    TemackerTheme {
        Row {
            StatusChip(text = "For you", tone = ChipTone.WARNING)
            StatusChip(text = "Not sent", tone = ChipTone.DANGER, modifier = Modifier.padding(start = 8.dp))
            StatusChip(text = "2 days late", tone = ChipTone.DANGER, icon = Icons.Rounded.Schedule, modifier = Modifier.padding(start = 8.dp))
            StatusChip(text = "Due Fri", tone = ChipTone.NEUTRAL, icon = Icons.Rounded.Schedule, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
