package com.example.temacker.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme

// One row shape for Inbox, Roster, Load, Stuck, Pulse, Queue, Handoff and Switch project instead
// of each rebuilding its own Row — see phase-8-ui-redesign.md §3. A 64dp minimum height matches
// the ui-ux-pro-max skill's touch-target guidance.
@Composable
fun ListRow(
    headline: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = Spacing.m, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.let {
            it()
            Spacer(Modifier.width(Spacing.s))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(headline, style = MaterialTheme.typography.bodyLarge)
            supporting?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Ink500) }
        }
        trailing?.invoke(this)
    }
}

// A rounded card grouping several ListRows/settings rows — Roster's "Manage roles" entry, Profile's
// settings, Manage roles' permission groups all used a bare Card before; this names the pattern.
@Composable
fun InsetGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth(),
        content = content
    )
}

@Preview(showBackground = true)
@Composable
private fun ListRowPreview() {
    TemackerTheme {
        InsetGroup {
            ListRow(
                headline = "Your data",
                leading = { Avatar(name = "You", size = 36.dp) },
                trailing = { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500) },
                onClick = {}
            )
            ListRow(headline = "Plan & limits", supporting = "Free — for a single team", onClick = {})
        }
    }
}
