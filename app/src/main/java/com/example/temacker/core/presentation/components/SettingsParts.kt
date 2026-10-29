package com.example.temacker.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme

// Grouped-list pieces shared by You, Team and Manage roles so they read as one family of screens.
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = Ink500,
        modifier = modifier.padding(start = Spacing.xxs, top = Spacing.m, bottom = Spacing.xs)
    )
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color, wash: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(36.dp).background(wash, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun RowChevron() = Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500)

// Starts past a 36dp badge so the line sits under the row text, not under the icon.
@Composable
fun InsetDivider(start: androidx.compose.ui.unit.Dp = 64.dp) = HorizontalDivider(color = Line, modifier = Modifier.padding(start = start))

@Preview(showBackground = true, backgroundColor = 0xFFFAF9F6)
@Composable
private fun SettingsPartsPreview() {
    TemackerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionLabel("Settings")
            InsetGroup {
                ListRow(
                    headline = "Notifications",
                    supporting = "Handoff pushes and reminders",
                    leading = { IconBadge(Icons.Rounded.Notifications, AmberInk, AmberWash) },
                    trailing = { RowChevron() },
                    onClick = {}
                )
                InsetDivider()
                ListRow(headline = "Sign out", leading = { IconBadge(Icons.Rounded.Notifications, AmberInk, AmberWash) }, onClick = {})
            }
        }
    }
}
