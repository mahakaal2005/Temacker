package com.example.temacker.feature_tasks.presentation.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.AppDestination
import com.example.temacker.core.presentation.components.AppScaffold
import com.example.temacker.core.presentation.components.SwitcherPill
import com.example.temacker.core.presentation.components.bottomBarContentPadding
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.PendingWrite
import com.example.temacker.feature_tasks.domain.model.PendingWriteStatus
import com.example.temacker.feature_tasks.domain.model.PendingWriteType
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BoardRoot(
    onNavigateToInbox: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToYou: () -> Unit,
    onNavigateToNewTask: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToIncoming: (String, String) -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToSwitchProject: () -> Unit,
    viewModel: BoardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            BoardEvent.NavigateToNewTask -> onNavigateToNewTask()
            BoardEvent.NavigateToQueue -> onNavigateToQueue()
            is BoardEvent.NavigateToTaskDetail -> onNavigateToTaskDetail(event.taskId)
            is BoardEvent.NavigateToIncoming -> onNavigateToIncoming(event.taskId, event.handoffId)
        }
    }

    BoardScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToInbox = onNavigateToInbox,
        onNavigateToTeam = onNavigateToTeam,
        onNavigateToYou = onNavigateToYou,
        onNavigateToSwitchProject = onNavigateToSwitchProject
    )
}

@Composable
fun BoardScreen(
    state: BoardState,
    onAction: (BoardAction) -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToYou: () -> Unit,
    onNavigateToSwitchProject: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Undo is offered while the notice is showing; either way the notice is cleared afterwards.
    LaunchedEffect(state.queuedNotice) {
        val notice = state.queuedNotice ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Queued. It sends when you're online.",
            actionLabel = "Undo",
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) onAction(BoardAction.OnQueuedUndo(notice.writeId)) else onAction(BoardAction.OnQueuedNoticeDismissed)
    }
    AppScaffold(
        selected = AppDestination.BOARD,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        onSelect = { destination ->
            when (destination) {
                AppDestination.BOARD -> Unit
                AppDestination.INBOX -> onNavigateToInbox()
                AppDestination.TEAM -> onNavigateToTeam()
                AppDestination.YOU -> onNavigateToYou()
            }
        },
        header = {
            // Header never disappears while loading — SwitcherPill shows its own skeleton then.
            SwitcherPill(
                projectName = state.projectSummary?.name,
                metaLine = state.projectSummary?.let { "${it.memberCount} members · ${it.roleName}" },
                hasOtherProjects = state.projectSummary?.hasOtherProjects ?: false,
                onClick = onNavigateToSwitchProject
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottomBarContentPadding(padding))) {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                        TextButton(onClick = { onAction(BoardAction.OnErrorDismissed) }) { Text("Dismiss") }
                    }
                }

                val boardTasks = state.boardTasks()
                val tabs = TaskStatus.entries.toList()
                TabRow(selectedTabIndex = tabs.indexOf(state.selectedTab)) {
                    tabs.forEach { status ->
                        val count = boardTasks.count { it.status == status }
                        Tab(
                            selected = state.selectedTab == status && !state.isFilteredToPending,
                            onClick = { onAction(BoardAction.OnTabSelected(status)) },
                            text = { Text("${status.label()} ($count)") }
                        )
                    }
                }

                syncStrip(
                    isOnline = state.isOnline,
                    queued = state.pendingWrites.count { it.status == PendingWriteStatus.PENDING },
                    failed = state.pendingWrites.count { it.status == PendingWriteStatus.FAILED }
                )?.let { SyncStripRow(it, isOffline = !state.isOnline, onClick = { onAction(BoardAction.OnSyncStripClick) }) }

                if (state.queuedInOtherProjects > 0) {
                    OtherProjectsQueueStrip(count = state.queuedInOtherProjects, onClick = onNavigateToSwitchProject)
                }

                if (state.pendingHandoffs.isNotEmpty()) {
                    WaitingOnYouStrip(
                        count = state.pendingHandoffs.size,
                        isActive = state.isFilteredToPending,
                        onClick = { onAction(BoardAction.OnWaitingOnYouClick) }
                    )
                }

                if (state.isLoading) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    val visibleTasks = if (state.isFilteredToPending) {
                        val pendingTaskIds = state.pendingHandoffs.map { it.taskId }.toSet()
                        boardTasks.filter { it.id in pendingTaskIds }
                    } else {
                        boardTasks.filter { it.status == state.selectedTab }
                    }
                    if (visibleTasks.isEmpty()) {
                        BoardEmptyState(canCreateTask = state.canCreateTask, onAddClick = { onAction(BoardAction.OnFabClick) })
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                            items(visibleTasks, key = { it.id }) { task ->
                                val sync = state.pendingWrites.syncStateOf(task.id)
                                // A queued answer already exists for this offer, so it is no longer "for you".
                                val pendingHandoff = state.pendingHandoffs.firstOrNull { it.taskId == task.id }.takeIf { sync == null }
                                TaskCard(
                                    task = task,
                                    isOfferedToYou = pendingHandoff != null,
                                    sync = sync,
                                    onClick = {
                                        if (pendingHandoff != null) {
                                            onAction(BoardAction.OnIncomingTaskClick(task.id, pendingHandoff.id))
                                        } else {
                                            onAction(BoardAction.OnTaskClick(task.id))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (state.canCreateTask) {
                FloatingActionButton(
                    onClick = { onAction(BoardAction.OnFabClick) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New task")
                }
            }
        }
    }
}

private fun TaskStatus.label(): String = when (this) {
    TaskStatus.TODO -> "To do"
    TaskStatus.DOING -> "Doing"
    TaskStatus.DONE -> "Done"
}

@Composable
private fun OtherProjectsQueueStrip(count: Int, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().clickableRow(onClick).padding(12.dp)) {
            Text(
                "$count queued change${if (count == 1) "" else "s"} in your other projects",
                style = MaterialTheme.typography.bodyLarge
            )
            Text("Switch projects to review or retry them", style = MaterialTheme.typography.bodySmall, color = Ink500)
        }
    }
}

@Composable
private fun WaitingOnYouStrip(count: Int, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isActive) AmberWash else MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .then(Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = AmberWash, modifier = Modifier.size(32.dp)) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$count", color = AmberInk, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f).clickableRow(onClick)) {
                Text("$count handoff${if (count == 1) "" else "s"} waiting on you", style = MaterialTheme.typography.bodyLarge)
                Text("Tap to see only these", style = MaterialTheme.typography.bodySmall, color = Ink500)
            }
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier = this.clickable(onClick = onClick)

@Composable
private fun TaskCard(task: Task, isOfferedToYou: Boolean, sync: TaskSyncState?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isOfferedToYou) Amber else Line)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(task.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                if (isOfferedToYou) {
                    Surface(color = AmberWash, shape = MaterialTheme.shapes.small) {
                        Text("For you", style = MaterialTheme.typography.labelSmall, color = AmberInk, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                } else if (sync != null) {
                    SyncChip(sync)
                } else {
                    task.dueLabel()?.let { due ->
                        Text(due, style = MaterialTheme.typography.labelSmall, color = Ink500)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = TealWash, modifier = Modifier.size(28.dp)) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(task.holderDisplayName.take(2).uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TealInk)
                    }
                }
                Text(
                    text = "Held by ${task.holderDisplayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink500,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun SyncStripRow(strip: SyncStrip, isOffline: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isOffline) MaterialTheme.colorScheme.surfaceVariant else AmberWash,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(strip.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(strip.detail, style = MaterialTheme.typography.bodySmall, color = Ink500)
        }
    }
}

@Composable
private fun SyncChip(sync: TaskSyncState) {
    val failed = sync == TaskSyncState.NOT_SENT
    Surface(color = if (failed) MaterialTheme.colorScheme.errorContainer else AmberWash, shape = MaterialTheme.shapes.small) {
        Text(
            text = if (failed) "Not sent" else "Queued",
            style = MaterialTheme.typography.labelSmall,
            color = if (failed) MaterialTheme.colorScheme.onErrorContainer else AmberInk,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BoardEmptyState(canCreateTask: Boolean, onAddClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Nothing to hand over yet", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Add the first task and it starts with you. You'll hold it until you pass it to someone who accepts.",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500,
            modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
        )
        if (canCreateTask) {
            TextButton(onClick = onAddClick, modifier = Modifier.padding(top = 16.dp)) {
                Text("Add the first task")
            }
        }
    }
}

private fun Task.dueLabel(): String? = dueDate?.let {
    SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(it))
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenLoadingPreview() {
    TemackerTheme {
        BoardScreen(state = BoardState(isLoading = true), onAction = {}, onNavigateToInbox = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenEmptyPreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(isLoading = false, canCreateTask = true, selectedTab = TaskStatus.TODO),
            onAction = {},
            onNavigateToInbox = {}, onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenWithTasksPreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(
                isLoading = false,
                canCreateTask = true,
                selectedTab = TaskStatus.DOING,
                tasks = listOf(
                    Task("t1", "p1", "Sponsor deck — final pass", null, "u2", "Mei-Ling Chow", TaskStatus.DOING, null, 3, "u1", "Priya Raman", 0, 0),
                    Task("t2", "p1", "Stage plan sign-off", null, "u1", "You", TaskStatus.DOING, null, 1, "u1", "You", 0, 0)
                ),
                pendingHandoffs = listOf(
                    Handoff("h1", "t1", "u2", "Mei-Ling Chow", "u1", "You", null, com.example.temacker.feature_tasks.domain.model.HandoffStatus.OFFERED, null, 0, null)
                )
            ),
            onAction = {},
            onNavigateToInbox = {}, onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {},
        )
    }
}

private val previewTask = Task("t1", "p1", "Sponsor deck — final pass", null, "u2", "Mei-Ling Chow", TaskStatus.TODO, null, 3, "u1", "Priya Raman", 0, 0)

private fun previewWrite(id: Long, type: PendingWriteType, taskId: String, status: PendingWriteStatus, draft: Task? = null) =
    PendingWrite(id, type, "p1", taskId, "Sponsor deck — final pass", status, 0, null, id, draft)

@Preview(showBackground = true)
@Composable
private fun BoardScreenOfflinePreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(isLoading = false, isOnline = false, canCreateTask = true, tasks = listOf(previewTask)),
            onAction = {}, onNavigateToInbox = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenOfflineQueuedPreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(
                isLoading = false, isOnline = false, canCreateTask = true, tasks = listOf(previewTask),
                pendingWrites = listOf(
                    previewWrite(1, PendingWriteType.MARK_DONE, "t1", PendingWriteStatus.PENDING),
                    previewWrite(2, PendingWriteType.CREATE_TASK, "t2", PendingWriteStatus.PENDING, previewTask.copy(id = "t2", title = "Book the venue", holderDisplayName = "You"))
                )
            ),
            onAction = {}, onNavigateToInbox = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenOtherProjectQueuePreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(isLoading = false, canCreateTask = true, tasks = listOf(previewTask), queuedInOtherProjects = 2),
            onAction = {}, onNavigateToInbox = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenSendingPreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(
                isLoading = false, tasks = listOf(previewTask),
                pendingWrites = listOf(previewWrite(1, PendingWriteType.MARK_DONE, "t1", PendingWriteStatus.PENDING))
            ),
            onAction = {}, onNavigateToInbox = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenFailedPreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(
                isLoading = false, tasks = listOf(previewTask),
                pendingWrites = listOf(previewWrite(1, PendingWriteType.ACCEPT, "t1", PendingWriteStatus.FAILED))
            ),
            onAction = {}, onNavigateToInbox = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {}
        )
    }
}
