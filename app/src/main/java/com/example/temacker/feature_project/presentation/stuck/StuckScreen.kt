package com.example.temacker.feature_project.presentation.stuck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.domain.model.StuckHandoff
import com.example.temacker.core.presentation.components.ChipTone
import com.example.temacker.core.presentation.components.EmptyState
import com.example.temacker.core.presentation.components.StatusChip
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun StuckRoot(viewModel: StuckViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StuckTabContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun StuckTabContent(
    state: StuckState,
    onAction: (StuckAction) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            state.error?.let { error ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onAction(StuckAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            when {
                state.isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                state.stuckHandoffs.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.Schedule,
                    title = "Nothing's been sitting unanswered",
                    body = "Handoffs that go quiet for a while will show up here so nothing gets forgotten."
                )
                else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                    items(state.stuckHandoffs, key = { it.handoffId }) { stuck ->
                        StuckHandoffRow(stuck)
                        HorizontalDivider(color = Line)
                    }
                }
            }
        }
    }
}

@Composable
private fun StuckHandoffRow(stuck: StuckHandoff) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(stuck.taskTitle, style = MaterialTheme.typography.bodyLarge)
        Text(
            "${stuck.fromDisplayName} → ${stuck.toDisplayName}",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500
        )
        // Amber under 48h, coral at/after — matches the notification factory's own waiting-nudge threshold.
        StatusChip(
            text = "Unanswered for ${stuck.hoursStuck}h",
            tone = if (stuck.hoursStuck >= 48) ChipTone.DANGER else ChipTone.WARNING,
            modifier = Modifier.padding(top = Spacing.xxs)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StuckTabContentPreview() {
    TemackerTheme {
        StuckTabContent(
            state = StuckState(
                isLoading = false,
                stuckHandoffs = listOf(
                    StuckHandoff("h1", "t1", "Print vendor quotes", "Priya Raman", "Daniel Osei", 0L, hoursStuck = 30),
                    StuckHandoff("h2", "t2", "Book the venue", "Daniel Osei", "Priya Raman", 0L, hoursStuck = 26)
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StuckTabContentLoadingPreview() {
    TemackerTheme {
        StuckTabContent(state = StuckState(isLoading = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun StuckTabContentEmptyPreview() {
    TemackerTheme {
        StuckTabContent(state = StuckState(isLoading = false, stuckHandoffs = emptyList()), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun StuckTabContentErrorPreview() {
    TemackerTheme {
        StuckTabContent(
            state = StuckState(
                isLoading = false,
                stuckHandoffs = listOf(
                    StuckHandoff("h1", "t1", "Print vendor quotes", "Priya Raman", "Daniel Osei", 0L, hoursStuck = 30)
                ),
                error = UiText.DynamicString("Couldn't load stuck handoffs. Check your connection and try again.")
            ),
            onAction = {}
        )
    }
}
