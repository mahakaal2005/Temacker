package com.example.temacker.feature_project.presentation.pulse

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.domain.model.TeamEvent
import com.example.temacker.core.domain.model.TeamEventType
import com.example.temacker.core.presentation.components.EmptyState
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun PulseRoot(viewModel: PulseViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PulseTabContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun PulseTabContent(
    state: PulseState,
    onAction: (PulseAction) -> Unit
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
                    TextButton(onClick = { onAction(PulseAction.OnErrorDismissed) }) { Text("Dismiss") }
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
                state.events.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.Timeline,
                    title = "It's quiet so far",
                    body = "Created, offered, accepted and done events will appear here."
                )
                else -> {
                    // Kotlin's groupBy keeps first-seen key order, which matches the list's existing
                    // newest-first sort as long as same-day events stay contiguous.
                    val grouped = state.events.groupBy { it.dayLabel() }
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                        grouped.forEach { (day, events) ->
                            item(key = "header-$day") {
                                Text(
                                    day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Ink500,
                                    modifier = Modifier.padding(top = Spacing.m, bottom = Spacing.xxs)
                                )
                            }
                            events.forEach { event ->
                                item(key = event.id) {
                                    PulseEventRow(event)
                                    HorizontalDivider(color = Line)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun TeamEventType.describe(): String = when (this) {
    TeamEventType.TASK_CREATED -> "created"
    TeamEventType.TASK_DELETED -> "deleted"
    TeamEventType.HANDOFF_OFFERED -> "offered a hand-off for"
    TeamEventType.HANDOFF_ACCEPTED -> "accepted a hand-off for"
    TeamEventType.HANDOFF_DECLINED -> "declined a hand-off for"
    TeamEventType.TASK_MARKED_DONE -> "marked done"
}

private fun TeamEventType.icon(): ImageVector = when (this) {
    TeamEventType.TASK_CREATED -> Icons.Rounded.AddCircle
    TeamEventType.TASK_DELETED -> Icons.Rounded.Delete
    TeamEventType.HANDOFF_OFFERED -> Icons.Rounded.Send
    TeamEventType.HANDOFF_ACCEPTED -> Icons.Rounded.CheckCircle
    TeamEventType.HANDOFF_DECLINED -> Icons.Rounded.Cancel
    TeamEventType.TASK_MARKED_DONE -> Icons.Rounded.TaskAlt
}

private val dayHeaderFormat = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())

private fun TeamEvent.dayLabel(): String = when {
    DateUtils.isToday(at) -> "Today"
    DateUtils.isToday(at + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
    else -> dayHeaderFormat.format(java.util.Date(at))
}

@Composable
private fun PulseEventRow(event: TeamEvent) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = NeutralWash, modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(event.type.icon(), contentDescription = null, tint = TealInk, modifier = Modifier.size(16.dp))
            }
        }
        Column(modifier = Modifier.padding(start = Spacing.s)) {
            Text(
                "${event.byDisplayName} ${event.type.describe()} \"${event.taskTitle}\"",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                DateUtils.getRelativeTimeSpanString(event.at).toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PulseTabContentPreview() {
    TemackerTheme {
        PulseTabContent(
            state = PulseState(
                isLoading = false,
                events = listOf(
                    TeamEvent("e1", TeamEventType.TASK_CREATED, "Print vendor quotes", "Priya Raman", System.currentTimeMillis() - 3_600_000),
                    TeamEvent("e2", TeamEventType.HANDOFF_ACCEPTED, "Book the venue", "Daniel Osei", System.currentTimeMillis() - 7_200_000)
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PulseTabContentLoadingPreview() {
    TemackerTheme {
        PulseTabContent(state = PulseState(isLoading = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PulseTabContentEmptyPreview() {
    TemackerTheme {
        PulseTabContent(state = PulseState(isLoading = false, events = emptyList()), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PulseTabContentErrorPreview() {
    TemackerTheme {
        PulseTabContent(
            state = PulseState(
                isLoading = false,
                events = listOf(
                    TeamEvent("e1", TeamEventType.TASK_CREATED, "Print vendor quotes", "Priya Raman", System.currentTimeMillis())
                ),
                error = UiText.DynamicString("Couldn't load activity. Check your connection and try again.")
            ),
            onAction = {}
        )
    }
}
