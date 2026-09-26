package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme

// One empty state instead of 8 ad-hoc centered Columns across the app — every case in
// phase-8-ui-redesign.md §8 gets an icon, a title, a body, and (usually) a next step, never just
// a grey line with nothing to do about it.
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    primaryLabel: String? = null,
    onPrimaryClick: (() -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = NeutralWash, modifier = Modifier.size(56.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = null, tint = Ink500, modifier = Modifier.size(28.dp))
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.m)
        )
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xs)
        )
        if (primaryLabel != null && onPrimaryClick != null) {
            Button(onClick = onPrimaryClick, modifier = Modifier.fillMaxWidth().padding(top = Spacing.l)) {
                Text(primaryLabel)
            }
        }
        if (secondaryLabel != null && onSecondaryClick != null) {
            TextButton(onClick = onSecondaryClick, modifier = Modifier.padding(top = Spacing.xxs)) {
                Text(secondaryLabel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateWithCtaPreview() {
    TemackerTheme {
        EmptyState(
            icon = Icons.Rounded.Groups,
            title = "It's just you so far",
            body = "Share the invite code, and teammates can join in seconds.",
            primaryLabel = "Copy invite code",
            onPrimaryClick = {},
            secondaryLabel = "Share",
            onSecondaryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStateNoCtaPreview() {
    TemackerTheme {
        EmptyState(icon = Icons.Rounded.Groups, title = "It's quiet so far", body = "Created, offered, accepted and done events will appear here.")
    }
}
