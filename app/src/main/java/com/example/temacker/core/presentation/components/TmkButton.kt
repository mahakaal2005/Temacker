package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Coral
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme

// Primary/secondary/destructive/text in one place — screens mixed Button/OutlinedButton/TextButton
// for the same job before (e.g. the empty-state CTA that should have been primary was a
// TextButton). A built-in loading state swaps the label for a spinner without the button
// resizing, so nothing jumps or reflows.
enum class TmkButtonVariant { PRIMARY, SECONDARY, DESTRUCTIVE, TEXT }

@Composable
fun TmkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TmkButtonVariant = TmkButtonVariant.PRIMARY,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val content: @Composable () -> Unit = {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColorFor(variant)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon?.let {
                        Icon(it, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = Spacing.xxs))
                    }
                    Text(text)
                }
            }
        }
    }
    val buttonModifier = modifier.fillMaxWidth().height(56.dp)
    val isEnabled = enabled && !isLoading
    when (variant) {
        TmkButtonVariant.PRIMARY -> Button(onClick = onClick, enabled = isEnabled, modifier = buttonModifier) { content() }
        TmkButtonVariant.SECONDARY -> OutlinedButton(onClick = onClick, enabled = isEnabled, modifier = buttonModifier) { content() }
        TmkButtonVariant.DESTRUCTIVE -> Button(
            onClick = onClick,
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = MaterialTheme.colorScheme.onError),
            modifier = buttonModifier
        ) { content() }
        TmkButtonVariant.TEXT -> TextButton(onClick = onClick, enabled = isEnabled, modifier = modifier) { content() }
    }
}

@Composable
private fun LocalContentColorFor(variant: TmkButtonVariant) = when (variant) {
    TmkButtonVariant.PRIMARY -> MaterialTheme.colorScheme.onPrimary
    TmkButtonVariant.DESTRUCTIVE -> MaterialTheme.colorScheme.onError
    TmkButtonVariant.SECONDARY, TmkButtonVariant.TEXT -> MaterialTheme.colorScheme.primary
}

@Preview(showBackground = true)
@Composable
private fun TmkButtonPreview() {
    TemackerTheme {
        Column(modifier = Modifier.padding(Spacing.m)) {
            TmkButton(text = "Create task", onClick = {}, icon = Icons.Rounded.Add)
            TmkButton(text = "Cancel", onClick = {}, variant = TmkButtonVariant.SECONDARY, modifier = Modifier.padding(top = Spacing.xs))
            TmkButton(text = "Delete task", onClick = {}, variant = TmkButtonVariant.DESTRUCTIVE, modifier = Modifier.padding(top = Spacing.xs))
            TmkButton(text = "Sending…", onClick = {}, isLoading = true, modifier = Modifier.padding(top = Spacing.xs))
        }
    }
}
