package com.example.temacker.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme

// One bottom-sheet shape (28dp top radius, per the spec's --r-sheet) instead of each screen that
// wants one configuring ModalBottomSheet by hand. Used to turn Hand off into a sheet per
// phase-8-ui-redesign.md §7 — "you're still looking at the task."
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TmkSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(content = content)
    }
}

// Note: ModalBottomSheet previews can be unreliable in the Android Studio preview renderer
// depending on the Compose version; verify this one renders once a real build is available.
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TmkSheetPreview() {
    TemackerTheme {
        TmkSheet(onDismissRequest = {}) {
            Text("Hand off to", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(Spacing.l))
        }
    }
}
