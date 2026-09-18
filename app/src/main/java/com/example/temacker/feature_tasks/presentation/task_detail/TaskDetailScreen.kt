package com.example.temacker.feature_tasks.presentation.task_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.temacker.core.presentation.designsystem.Teal
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TaskDetailRoot(
    taskId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHandoff: (String) -> Unit,
    viewModel: TaskDetailViewModel = koinViewModel(parameters = { parametersOf(taskId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TaskDetailEvent.NavigateBack -> onNavigateBack()
            is TaskDetailEvent.NavigateToHandoff -> onNavigateToHandoff(event.taskId)
        }
    }

    TaskDetailScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(state: TaskDetailState, onAction: (TaskDetailAction) -> Unit, onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(state.task?.title ?: "Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    Box {
                        IconButton(onClick = { onAction(TaskDetailAction.OnMoreClick) }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = state.isMenuVisible, onDismissRequest = { onAction(TaskDetailAction.OnDismissMenu) }) {
                            val previousHolder = state.trail.filter { it.status == HandoffStatus.ACCEPTED }.maxByOrNull { it.respondedAt ?: 0L }
                            if (state.isCurrentUserHolder && previousHolder != null) {
                                DropdownMenuItem(
                                    text = { Text("Hand back to ${previousHolder.fromDisplayName}") },
                                    onClick = { onAction(TaskDetailAction.OnHandBackToPreviousClick) }
                                )
                            }
                            if (state.isCurrentUserHolder && state.task?.status != TaskStatus.DONE) {
                                DropdownMenuItem(text = { Text("Mark done") }, onClick = { onAction(TaskDetailAction.OnMarkDoneClick) })
                            }
                            if (state.canDelete || state.isCurrentUserHolder) {
                                DropdownMenuItem(
                                    text = { Text("Delete task", color = MaterialTheme.colorScheme.error) },
                                    onClick = { onAction(TaskDetailAction.OnDeleteClick) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(TaskDetailAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            if (state.isLoading || state.task == null) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val task = state.task
                Column(modifier = Modifier.weight(1f).padding(horizontal = 18.dp)) {
                    HolderCard(task = task)
                    Text("Baton trail", style = MaterialTheme.typography.labelMedium, color = Ink500, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
                    state.trail.forEach { handoff -> TrailEntry(handoff) }
                    TrailEntry(
                        Handoff(
                            id = "created",
                            taskId = task.id,
                            fromUid = task.createdByUid,
                            fromDisplayName = task.createdByDisplayName,
                            toUid = task.createdByUid,
                            toDisplayName = task.createdByDisplayName,
                            note = null,
                            status = HandoffStatus.ACCEPTED,
                            declineReason = null,
                            offeredAt = task.createdAt,
                            respondedAt = task.createdAt
                        ),
                        isCreationEntry = true
                    )
                }

                if (state.isCurrentUserHolder && task.status != TaskStatus.DONE) {
                    Button(
                        onClick = { onAction(TaskDetailAction.OnHandOffClick) },
                        modifier = Modifier.fillMaxWidth().padding(18.dp)
                    ) {
                        Text("Hand off")
                    }
                }
            }
        }
    }

    if (state.isDeleteConfirmVisible) {
        AlertDialog(
            onDismissRequest = { onAction(TaskDetailAction.OnDismissDeleteConfirm) },
            title = { Text("Delete this task?") },
            text = { Text("${state.task?.title} and its handoff history will be gone permanently. The record that you deleted it stays in the project pulse.") },
            confirmButton = {
                TextButton(onClick = { onAction(TaskDetailAction.OnConfirmDeleteClick) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { onAction(TaskDetailAction.OnDismissDeleteConfirm) }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HolderCard(task: Task) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = AmberWash, modifier = Modifier.size(48.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(task.holderDisplayName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = AmberInk)
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text("Holding the baton", style = MaterialTheme.typography.labelSmall, color = Ink500)
            Text(task.holderDisplayName, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun TrailEntry(handoff: Handoff, isCreationEntry: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        val dotColor = when {
            isCreationEntry -> Amber
            handoff.status == HandoffStatus.ACCEPTED -> Teal
            handoff.status == HandoffStatus.OFFERED -> Ink500
            else -> Ink500
        }
        Surface(shape = CircleShape, color = dotColor, modifier = Modifier.size(10.dp).padding(top = 6.dp)) {}
        Column(modifier = Modifier.padding(start = 12.dp)) {
            val headline = when {
                isCreationEntry -> "${handoff.fromDisplayName} created this task"
                handoff.status == HandoffStatus.ACCEPTED -> "${handoff.toDisplayName} accepted from ${handoff.fromDisplayName}"
                handoff.status == HandoffStatus.OFFERED -> "Offered to ${handoff.toDisplayName} — waiting"
                else -> "${handoff.toDisplayName} declined — ${handoff.declineReason.orEmpty()}"
            }
            Text(headline, style = MaterialTheme.typography.bodyMedium)
            handoff.note?.let { Text("\"$it\"", style = MaterialTheme.typography.bodySmall, color = Ink500, modifier = Modifier.padding(top = 2.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskDetailScreenPreview() {
    TemackerTheme {
        TaskDetailScreen(
            state = TaskDetailState(
                isLoading = false,
                isCurrentUserHolder = true,
                canDelete = true,
                task = Task("t1", "p1", "Sponsor deck — final pass", null, "u3", "Mei-Ling Chow", TaskStatus.DOING, null, 3, "u1", "You", 0, 0),
                trail = listOf(
                    Handoff("h1", "t1", "u2", "Daniel Osei", "u3", "Mei-Ling Chow", "Design's done — needs a print quote before Friday.", HandoffStatus.ACCEPTED, null, 0, 1000),
                    Handoff("h2", "t1", "u1", "You", "u2", "Daniel Osei", null, HandoffStatus.ACCEPTED, null, 0, 500)
                )
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskDetailScreenLoadingPreview() {
    TemackerTheme {
        TaskDetailScreen(state = TaskDetailState(isLoading = true), onAction = {}, onNavigateBack = {})
    }
}
