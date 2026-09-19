package com.example.temacker.feature_tasks.presentation.queue

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
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
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            if (!state.isOnline) OfflineNote()

            when {
                state.isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                state.failed.isEmpty() && state.waiting.isEmpty() -> QueueEmptyState()

                else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                    if (state.failed.isNotEmpty()) {
                        item(key = "failed-header") { SectionHeader("Not sent · ${state.failed.size}") }
                        items(state.failed, key = { "f-${it.id}" }) { row -> QueueRow(row, onAction) }
                    }
                    if (state.waiting.isNotEmpty()) {
                        item(key = "waiting-header") { SectionHeader("Waiting to send · ${state.waiting.size}") }
                        items(state.waiting, key = { "w-${it.id}" }) { row -> QueueRow(row, onAction) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineNote() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text("You're offline", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("They send when you're back", style = MaterialTheme.typography.bodySmall, color = Ink500)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Ink500,
        modifier = Modifier.padding(start = 6.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun QueueRow(row: QueueRowUi, onAction: (QueueAction) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (row.isFailed) MaterialTheme.colorScheme.error else Line)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(row.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                StatusChip(isFailed = row.isFailed)
            }
            Text(row.taskTitle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
            Text(row.detail, style = MaterialTheme.typography.bodySmall, color = Ink500, modifier = Modifier.padding(top = 4.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                if (row.isFailed) TextButton(onClick = { onAction(QueueAction.OnRetryClick(row.id)) }) { Text("Retry") }
                TextButton(onClick = { onAction(QueueAction.OnDiscardClick(row.id)) }) { Text("Discard") }
            }
        }
    }
}

@Composable
private fun StatusChip(isFailed: Boolean) {
    Surface(color = if (isFailed) MaterialTheme.colorScheme.errorContainer else AmberWash, shape = MaterialTheme.shapes.small) {
        Text(
            text = if (isFailed) "Not sent" else "Queued",
            style = MaterialTheme.typography.labelSmall,
            color = if (isFailed) MaterialTheme.colorScheme.onErrorContainer else AmberInk,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun QueueEmptyState() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Nothing waiting", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Changes you make offline show up here until they send.",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500,
            modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
        )
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
