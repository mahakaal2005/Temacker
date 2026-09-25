package com.example.temacker.feature_tasks.presentation.task_detail

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.Avatar
import com.example.temacker.core.presentation.components.AvatarTone
import com.example.temacker.core.presentation.components.BatonTrail
import com.example.temacker.core.presentation.components.ChipTone
import com.example.temacker.core.presentation.components.ConfirmDialog
import com.example.temacker.core.presentation.components.LoadingState
import com.example.temacker.core.presentation.components.StatusChip
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.components.TmkButtonVariant
import com.example.temacker.core.presentation.components.TrailNode
import com.example.temacker.core.presentation.components.TrailNodeKind
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
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
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(TaskDetailAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            if (state.isLoading || state.task == null) {
                LoadingState()
            } else {
                val task = state.task
                // Scrollable — a long trail used to push Hand off / Mark done off-screen entirely.
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.l)) {
                    HolderCard(task = task)
                    task.description?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = Spacing.m))
                    }
                    task.dueDate?.let {
                        StatusChip(
                            text = "Due ${SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(it))}",
                            tone = ChipTone.NEUTRAL,
                            modifier = Modifier.padding(top = Spacing.s)
                        )
                    }
                    Text("Baton trail", style = MaterialTheme.typography.labelMedium, color = Ink500, modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.xs))
                    BatonTrail(nodes = trailNodesFor(task, state.trail))
                    Spacer(Modifier.height(Spacing.xxl))
                }

                // Mark done sits alongside Hand off instead of hiding in the ⋮ menu — it's a
                // primary action for whoever holds the task, not an overflow item.
                if (state.isCurrentUserHolder && task.status != TaskStatus.DONE) {
                    Row(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Spacing.l, vertical = Spacing.m),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                    ) {
                        TmkButton(text = "Mark done", onClick = { onAction(TaskDetailAction.OnMarkDoneClick) }, variant = TmkButtonVariant.SECONDARY, modifier = Modifier.weight(1f))
                        TmkButton(text = "Hand off", onClick = { onAction(TaskDetailAction.OnHandOffClick) }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (state.isDeleteConfirmVisible) {
        ConfirmDialog(
            title = "Delete this task?",
            text = "${state.task?.title} and its handoff history will be gone permanently. The record that you deleted it stays in the project pulse.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = { onAction(TaskDetailAction.OnConfirmDeleteClick) },
            onDismiss = { onAction(TaskDetailAction.OnDismissDeleteConfirm) }
        )
    }
}

@Composable
private fun HolderCard(task: Task) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
        Avatar(name = task.holderDisplayName, size = 48.dp, tone = AvatarTone.ACCENT)
        Column(modifier = Modifier.padding(start = Spacing.s)) {
            Text("Holding the baton", style = MaterialTheme.typography.labelSmall, color = Ink500)
            Text(task.holderDisplayName, style = MaterialTheme.typography.titleMedium)
        }
    }
}

// Handoff/Task -> BatonTrail's generic TrailNode. state.trail is newest-first; for each ACCEPTED
// entry, "held" is measured against the next-more-recent entry's start (or now, for the current
// holder) — the closest a pure UI mapper can get to "how long they held it" from the existing
// offeredAt/respondedAt/createdAt fields, with no ViewModel change.
private fun trailNodesFor(task: Task, trail: List<Handoff>): List<TrailNode> {
    val nodes = trail.mapIndexed { index, handoff ->
        val heldUntil = if (index == 0) System.currentTimeMillis() else (trail[index - 1].respondedAt ?: trail[index - 1].offeredAt)
        when (handoff.status) {
            HandoffStatus.ACCEPTED -> {
                val start = handoff.respondedAt ?: handoff.offeredAt
                val days = ((heldUntil - start) / 86_400_000L).toInt()
                val heldLabel = if (days < 1) "held less than a day" else "held $days day${if (days == 1) "" else "s"}"
                TrailNode(
                    headline = "${handoff.toDisplayName} accepted from ${handoff.fromDisplayName}",
                    detail = "${absoluteDate(start)} · $heldLabel",
                    note = handoff.note,
                    kind = TrailNodeKind.ACCEPTED
                )
            }
            HandoffStatus.OFFERED -> TrailNode(
                headline = "Offered to ${handoff.toDisplayName} — waiting",
                detail = "${DateUtils.getRelativeTimeSpanString(handoff.offeredAt)} ago",
                note = handoff.note,
                kind = TrailNodeKind.OFFERED_WAITING
            )
            HandoffStatus.DECLINED -> TrailNode(
                headline = "${handoff.toDisplayName} declined",
                detail = handoff.respondedAt?.let(::absoluteDate),
                note = handoff.declineReason,
                kind = TrailNodeKind.DECLINED
            )
        }
    }
    return nodes + TrailNode(
        headline = "${task.createdByDisplayName} created this task",
        detail = absoluteDate(task.createdAt),
        note = null,
        kind = TrailNodeKind.CREATED
    )
}

private fun absoluteDate(millis: Long): String = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis))

@Preview(showBackground = true)
@Composable
private fun TaskDetailScreenPreview() {
    TemackerTheme {
        TaskDetailScreen(
            state = TaskDetailState(
                isLoading = false,
                isCurrentUserHolder = true,
                canDelete = true,
                task = Task(
                    "t1", "p1", "Sponsor deck — final pass",
                    "Needs the sponsor's final sign-off before it goes to print.",
                    "u3", "Mei-Ling Chow", TaskStatus.DOING, System.currentTimeMillis() + 2 * 86_400_000L, 3, "u1", "You", 0, 0
                ),
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

@Preview(showBackground = true)
@Composable
private fun TaskDetailScreenLongTrailPreview() {
    TemackerTheme {
        TaskDetailScreen(
            state = TaskDetailState(
                isLoading = false,
                isCurrentUserHolder = true,
                canDelete = false,
                task = Task("t1", "p1", "Venue walkthrough", null, "u1", "You", TaskStatus.DOING, null, 6, "u1", "You", 0, 0),
                trail = List(8) { i ->
                    Handoff("h$i", "t1", "u${i + 1}", "Member ${i + 1}", "u${i + 2}", "Member ${i + 2}", null, HandoffStatus.ACCEPTED, null, 0, 1000L * i)
                }
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
