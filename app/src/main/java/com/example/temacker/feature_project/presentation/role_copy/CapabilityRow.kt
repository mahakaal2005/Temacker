package com.example.temacker.feature_project.presentation.role_copy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink700
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TemackerTheme

// One accessibility stop per row ("Invite members, needs a role") instead of an icon stop plus a text stop.
@Composable
fun CapabilityRow(label: String, allowed: Boolean, lockedColor: Color = Ink700) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label, ${if (allowed) "allowed" else "needs a role"}"
        }
    ) {
        Icon(
            imageVector = if (allowed) Icons.Default.Check else Icons.Default.Lock,
            contentDescription = null,
            tint = if (allowed) TealInk else Ink500,
            modifier = Modifier.size(18.dp)
        )
        Text(label, style = MaterialTheme.typography.bodyLarge, color = if (allowed) TealInk else lockedColor)
    }
}

@Preview(showBackground = true)
@Composable
private fun CapabilityRowPreview() {
    TemackerTheme {
        Column {
            CapabilityRow("Create tasks", allowed = true)
            CapabilityRow("Invite members", allowed = false)
        }
    }
}
