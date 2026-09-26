package com.example.temacker.feature_tasks.presentation.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.ChipTone
import com.example.temacker.core.presentation.components.EmptyState
import com.example.temacker.core.presentation.components.InfoStrip
import com.example.temacker.core.presentation.components.InsetGroup
import com.example.temacker.core.presentation.components.ListRow
import com.example.temacker.core.presentation.components.LoadingState
import com.example.temacker.core.presentation.components.StatusChip
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun QueueRoot(
    onNavigateBack: () -> Unit,
    viewModel: QueueViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QueueScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(state: QueueState, onAction: (QueueAction) -> Unit, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Queued changes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            InfoStrip(
                visible = !state.isOnline,
                title = "You're offline",
                detail = "They send when you're back",
                background = MaterialTheme.colorScheme.surfaceVariant,
                leading = { Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = Ink500, modifier = Modifier.padding(end = Spacing.s)) }
            )

            when {
                state.isLoading -> LoadingState()
                state.failed.isEmpty() && state.waiting.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.CloudOff,
                    title = "Nothing waiting",
                    body = "Changes you make offline show up here until they send."
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.s)) {
                    if (state.failed.isNotEmpty()) {
                        item(key = "failed-header") { SectionHeader("Not sent · ${state.failed.size}") }
                        item(key = "failed-group") {
                            InsetGroup(modifier = Modifier.padding(bottom = Spacing.s)) {
                                state.failed.forEachIndexed { index, row -> QueueRow(row, onAction, showDivider = index != state.failed.lastIndex) }
                            }
                        }
                    }
                    if (state.waiting.isNotEmpty()) {
                        item(key = "waiting-header") { SectionHeader("Waiting to send · ${state.waiting.size}") }
                        item(key = "waiting-group") {
                            InsetGroup {
                                state.waiting.forEachIndexed { index, row -> QueueRow(row, onAction, showDivider = index != state.waiting.lastIndex) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Ink500,
        modifier = Modifier.padding(start = Spacing.xs, top = Spacing.m, bottom = Spacing.xxs)
    )
}

@Composable
private fun QueueRow(row: QueueRowUi, onAction: (QueueAction) -> Unit, showDivider: Boolean) {
    Column {
        ListRow(
            headline = row.title,
            supporting = "${row.taskTitle} · ${row.detail}",
            trailing = { StatusChip(text = if (row.isFailed) "Not sent" else "Queued", tone = if (row.isFailed) ChipTone.DANGER else ChipTone.WARNING) }
        )
        Row(modifier = Modifier.fillMaxWidth().padding(end = Spacing.xs), horizontalArrangement = Arrangement.End) {
            if (row.isFailed) TextButton(onClick = { onAction(QueueAction.OnRetryClick(row.id)) }) { Text("Retry") }
            TextButton(onClick = { onAction(QueueAction.OnDiscardClick(row.id)) }) { Text("Discard") }
        }
        if (showDivider) HorizontalDivider(color = Line)
    }
}

private val previewFailed = listOf(
    QueueRowUi(1, "Accept handoff", "Sponsor deck — final pass", "It changed before this could send.", true)
)
private val previewWaiting = listOf(
    QueueRowUi(2, "Mark done", "Stage plan sign-off", "Saved 2 min ago", false),
    QueueRowUi(3, "New task", "Book the venue", "Saved just now", false)
)

@Preview(showBackground = true)
@Composable
private fun QueueScreenLoadingPreview() {
    TemackerTheme { QueueScreen(state = QueueState(isLoading = true), onAction = {}, onNavigateBack = {}) }
}

@Preview(showBackground = true)
@Composable
private fun QueueScreenEmptyPreview() {
    TemackerTheme { QueueScreen(state = QueueState(isLoading = false), onAction = {}, onNavigateBack = {}) }
}

@Preview(showBackground = true)
@Composable
private fun QueueScreenWaitingPreview() {
    TemackerTheme { QueueScreen(state = QueueState(waiting = previewWaiting, isOnline = false, isLoading = false), onAction = {}, onNavigateBack = {}) }
}

@Preview(showBackground = true)
@Composable
private fun QueueScreenFailedPreview() {
    TemackerTheme { QueueScreen(state = QueueState(failed = previewFailed, isLoading = false), onAction = {}, onNavigateBack = {}) }
}

@Preview(showBackground = true)
@Composable
private fun QueueScreenMixedPreview() {
    TemackerTheme { QueueScreen(state = QueueState(failed = previewFailed, waiting = previewWaiting, isOnline = false, isLoading = false), onAction = {}, onNavigateBack = {}) }
}
