package com.example.temacker.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.CoralInk
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.White

// White, bordered, rounded popup instead of Material's default lavender surface.
@Composable
fun TmkDropdownMenu(expanded: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(0.dp, 4.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = White,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Line),
        modifier = Modifier.padding(vertical = 2.dp)
    ) { content() }
}

// Sans-serif label with an optional one-line hint; destructive items go coral.
@Composable
fun TmkMenuItem(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    hint: String? = null,
    destructive: Boolean = false
) {
    val tint = if (destructive) CoralInk else Ink900
    DropdownMenuItem(
        text = {
            Column {
                Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = tint)
                hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Ink500) }
            }
        },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = if (destructive) CoralInk else Ink500, modifier = Modifier.size(22.dp)) } },
        onClick = onClick
    )
}

@Composable
fun TmkOverflowMenu(contentDescription: String, content: @Composable (dismiss: () -> Unit) -> Unit) {
    var open = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Box {
        IconButton(onClick = { open.value = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = contentDescription) }
        TmkDropdownMenu(expanded = open.value, onDismiss = { open.value = false }) { content { open.value = false } }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9F6)
@Composable
private fun TmkMenuPreview() {
    TemackerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TmkMenuItem("Start new cycle", onClick = {}, icon = Icons.Rounded.Autorenew, hint = "Hand off leadership and reset the board")
            TmkMenuItem("Remove from project", onClick = {}, destructive = true)
        }
    }
}
