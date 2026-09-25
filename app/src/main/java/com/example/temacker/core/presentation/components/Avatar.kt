package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.TemackerTheme

// Amber = the two meanings already used ad hoc across screens (Leader, "the holder"). Teal is
// everyone else. Not a new color scheme — just one Avatar instead of 6 hand-rolled copies of it.
enum class AvatarTone { NEUTRAL, ACCENT }

@Composable
fun Avatar(name: String, size: Dp = 36.dp, tone: AvatarTone = AvatarTone.NEUTRAL, modifier: Modifier = Modifier) {
    val (background, foreground) = when (tone) {
        AvatarTone.NEUTRAL -> TealWash to TealInk
        AvatarTone.ACCENT -> AmberWash to AmberInk
    }
    Surface(shape = CircleShape, color = background, modifier = modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initialsOf(name),
                style = if (size >= 48.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = foreground
            )
        }
    }
}

// "Mei-Ling Chow" -> "MC", not "ME" — first letter of the first and last word, not the first
// two characters. A single word ("You") falls back to its first two letters.
fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words.first().take(2).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarPreview() {
    TemackerTheme {
        Row {
            Avatar(name = "Mei-Ling Chow", size = 28.dp)
            Avatar(name = "Daniel Osei", size = 36.dp, modifier = Modifier.padding(start = 8.dp))
            Avatar(name = "You", size = 48.dp, tone = AvatarTone.ACCENT, modifier = Modifier.padding(start = 8.dp))
            Avatar(name = "Aisha Bello", size = 56.dp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
