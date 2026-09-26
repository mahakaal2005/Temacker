package com.example.temacker.core.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Coral
import com.example.temacker.core.presentation.designsystem.TemackerTheme

// One confirm/cancel dialog shape (28dp radius, per --r-sheet) replacing the 6 near-identical
// AlertDialogs across Task detail, Roster, Your data, Manage roles and Profile.
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    dismissLabel: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (isDestructive) Coral else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } }
    )
}

@Preview(showBackground = true)
@Composable
private fun ConfirmDialogPreview() {
    TemackerTheme {
        ConfirmDialog(
            title = "Delete this task?",
            text = "It and its handoff history will be gone permanently.",
            confirmLabel = "Delete",
            onConfirm = {},
            onDismiss = {},
            isDestructive = true
        )
    }
}
