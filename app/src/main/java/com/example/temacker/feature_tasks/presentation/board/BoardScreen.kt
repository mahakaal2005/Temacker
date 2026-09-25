package com.example.temacker.feature_tasks.presentation.board

import android.text.format.DateUtils
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.AppDestination
import com.example.temacker.core.presentation.components.AppScaffold
import com.example.temacker.core.presentation.components.Avatar
import com.example.temacker.core.presentation.components.ChipTone
import com.example.temacker.core.presentation.components.EmptyState
import com.example.temacker.core.presentation.components.InfoStrip
import com.example.temacker.core.presentation.components.SegmentedTabs
import com.example.temacker.core.presentation.components.SkeletonList
import com.example.temacker.core.presentation.components.StatusChip
import com.example.temacker.core.presentation.components.SwitcherPill
import com.example.temacker.core.presentation.components.bottomBarContentPadding
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.cardElevation
import com.example.temacker.core.presentation.designsystem.spatialDefault
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
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
        val lazyListState = rememberLazyListState()
        Box(modifier = Modifier.fillMaxSize().padding(bottomBarContentPadding(padding))) {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                state.error?.let { error ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xxs),
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

                // The waiting-on-you strip sits above the tabs so it survives every tab change,
                // and doubles as the exit from the filter it applies — see phase-8 §7.
                InfoStrip(
                    visible = state.pendingHandoffs.isNotEmpty(),
                    title = "${state.pendingHandoffs.size} handoff${if (state.pendingHandoffs.size == 1) "" else "s"} waiting on you",
                    detail = if (state.isFilteredToPending) "Showing only these · tap to show all" else "Tap to see only these",
                    background = if (state.isFilteredToPending) AmberWash else MaterialTheme.colorScheme.background,
                    leading = { WaitingCountBadge(state.pendingHandoffs.size) },
                    onClick = { onAction(BoardAction.OnWaitingOnYouClick) }
                )

                if (!state.isFilteredToPending) {
                    val tabs = TaskStatus.entries.toList()
                    SegmentedTabs(
                        items = tabs,
                        selected = state.selectedTab,
                        onSelect = { onAction(BoardAction.OnTabSelected(it)) },
                        label = { status -> "${status.label()} ${boardTasks.count { it.status == status }}" },
                        modifier = Modifier.padding(top = Spacing.xxs)
                    )

                    val sync = syncStrip(
                        isOnline = state.isOnline,
                        queued = state.pendingWrites.count { it.status == PendingWriteStatus.PENDING },
                        failed = state.pendingWrites.count { it.status == PendingWriteStatus.FAILED }
                    )
                    InfoStrip(
                        visible = sync != null,
                        title = sync?.title.orEmpty(),
                        detail = sync?.detail.orEmpty(),
                        background = if (!state.isOnline) MaterialTheme.colorScheme.surfaceVariant else AmberWash,
                        onClick = { onAction(BoardAction.OnSyncStripClick) }
                    )

                    InfoStrip(
                        visible = state.queuedInOtherProjects > 0,
                        title = "${state.queuedInOtherProjects} queued change${if (state.queuedInOtherProjects == 1) "" else "s"} in your other projects",
                        detail = "Switch projects to review or retry them",
                        onClick = onNavigateToSwitchProject
                    )
                }

                if (state.isLoading) {
                    SkeletonList()
                } else {
                    val now = remember { System.currentTimeMillis() }
                    val visibleTasks = if (state.isFilteredToPending) {
                        val pendingTaskIds = state.pendingHandoffs.map { it.taskId }.toSet()
                        boardTasks.filter { it.id in pendingTaskIds }
                    } else {
                        boardTasks.filter { it.status == state.selectedTab }
                    }
                    if (visibleTasks.isEmpty()) {
                        BoardEmptyState(
                            isFilteredToPending = state.isFilteredToPending,
                            selectedTab = state.selectedTab,
                            canCreateTask = state.canCreateTask,
                            onAddClick = { onAction(BoardAction.OnFabClick) },
                            onShowAllClick = { onAction(BoardAction.OnWaitingOnYouClick) }
                        )
                    } else {
                        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.s)) {
                            items(visibleTasks, key = { it.id }) { task ->
                                val sync = state.pendingWrites.syncStateOf(task.id)
                                // A queued answer already exists for this offer, so it is no longer "for you".
                                val offer = state.pendingHandoffs.firstOrNull { it.taskId == task.id }.takeIf { sync == null }
                                TaskCard(
                                    task = task,
                                    offer = offer,
                                    sync = sync,
                                    now = now,
                                    modifier = Modifier.animateItem(),
                                    onClick = {
                                        if (offer != null) {
                                            onAction(BoardAction.OnIncomingTaskClick(task.id, offer.id))
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
                // Compose-owned LazyListState driving a derived value — the one case the
                // android-compose-ui skill calls out for derivedStateOf.
                val fabExpanded by remember { derivedStateOf { lazyListState.firstVisibleItemIndex == 0 } }
                ExtendedFloatingActionButton(
                    text = { Text("New task") },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    onClick = { onAction(BoardAction.OnFabClick) },
                    expanded = fabExpanded,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(Spacing.l)
                )
            }
        }
    }
}

@Composable
private fun WaitingCountBadge(count: Int) {
    Surface(
        shape = CircleShape,
        color = AmberWash,
        modifier = Modifier.padding(end = Spacing.s)
    ) {
        Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
            Text("$count", style = MaterialTheme.typography.labelLarge, color = AmberInk)
        }
    }
}

private fun TaskStatus.label(): String = when (this) {
    TaskStatus.TODO -> "To do"
    TaskStatus.DOING -> "Doing"
    TaskStatus.DONE -> "Done"
}

@Composable
private fun TaskCard(task: Task, offer: Handoff?, sync: TaskSyncState?, now: Long, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOfferedToYou = offer != null
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, animationSpec = spatialDefault(), label = "taskCardPress")
    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .let { if (!isOfferedToYou) it.cardElevation() else it },
        colors = CardDefaults.cardColors(containerColor = if (isOfferedToYou) AmberWash else MaterialTheme.colorScheme.surface),
        border = if (isOfferedToYou) BorderStroke(1.dp, Amber) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(task.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                TaskTrailingChip(task, isOfferedToYou, sync, now)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = task.holderDisplayName, size = 28.dp)
                Text(
                    text = metaLineFor(task, offer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink500,
                    modifier = Modifier.padding(start = Spacing.xs)
                )
            }
        }
    }
}

@Composable
private fun TaskTrailingChip(task: Task, isOfferedToYou: Boolean, sync: TaskSyncState?, now: Long) {
    when {
        isOfferedToYou -> StatusChip("For you", ChipTone.WARNING)
        sync == TaskSyncState.NOT_SENT -> StatusChip("Not sent", ChipTone.DANGER)
        sync == TaskSyncState.QUEUED -> StatusChip("Queued", ChipTone.WARNING)
        task.isOverdue(now) -> StatusChip(task.lateLabel(now), ChipTone.DANGER, icon = Icons.Rounded.Schedule)
        task.dueDate != null -> task.dueLabel()?.let { StatusChip(it, ChipTone.NEUTRAL, icon = Icons.Rounded.Schedule) }
        else -> Unit
    }
}

@Composable
private fun BoardEmptyState(
    isFilteredToPending: Boolean,
    selectedTab: TaskStatus,
    canCreateTask: Boolean,
    onAddClick: () -> Unit,
    onShowAllClick: () -> Unit
) {
    when {
        isFilteredToPending -> EmptyState(
            icon = Icons.Rounded.CheckCircle,
            title = "You're all caught up",
            body = "Nobody's waiting on you right now.",
            primaryLabel = "Show all tasks",
            onPrimaryClick = onShowAllClick
        )
        selectedTab == TaskStatus.DONE -> EmptyState(
            icon = Icons.Rounded.CheckCircle,
            title = "Nothing finished yet",
            body = "Tasks land here when their holder marks them done."
        )
        canCreateTask -> EmptyState(
            icon = Icons.Rounded.Assignment,
            title = "Nothing to hand over yet",
            body = "Add the first task and it starts with you. You'll hold it until you pass it to someone who accepts.",
            primaryLabel = "Add the first task",
            onPrimaryClick = onAddClick
        )
        else -> EmptyState(
            icon = Icons.Rounded.Assignment,
            title = "No tasks here yet",
            body = "Tasks you're handed will show up here."
        )
    }
}

private fun Task.dueLabel(): String? = dueDate?.let {
    SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(it))
}

private fun Task.isOverdue(now: Long): Boolean = dueDate != null && dueDate < now && status != TaskStatus.DONE

private fun Task.lateLabel(now: Long): String {
    val days = (((now - (dueDate ?: now)) / 86_400_000L).toInt()).coerceAtLeast(1)
    return "$days day${if (days == 1) "" else "s"} late"
}

private fun metaLineFor(task: Task, offer: Handoff?): String = if (offer != null) {
    "${offer.fromDisplayName} offered this · ${DateUtils.getRelativeTimeSpanString(offer.offeredAt)}"
} else {
    "Held by ${task.holderDisplayName} · since ${SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(task.updatedAt))}"
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
private fun BoardScreenCannotCreateEmptyPreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(isLoading = false, canCreateTask = false, selectedTab = TaskStatus.TODO),
            onAction = {},
            onNavigateToInbox = {}, onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenDoneEmptyPreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(isLoading = false, canCreateTask = true, selectedTab = TaskStatus.DONE),
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
                    Task("t1", "p1", "Sponsor deck — final pass", null, "u2", "Mei-Ling Chow", TaskStatus.DOING, null, 3, "u1", "Priya Raman", 0, System.currentTimeMillis() - 3 * 86_400_000L),
                    Task("t2", "p1", "Stage plan sign-off", null, "u1", "You", TaskStatus.DOING, System.currentTimeMillis() - 2 * 86_400_000L, 1, "u1", "You", 0, 0)
                ),
                pendingHandoffs = listOf(
                    Handoff("h1", "t1", "u2", "Mei-Ling Chow", "u1", "You", null, HandoffStatus.OFFERED, null, System.currentTimeMillis() - 2 * 3600_000L, null)
                )
            ),
            onAction = {},
            onNavigateToInbox = {}, onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardScreenWaitingFilterEmptyPreview() {
    TemackerTheme {
        BoardScreen(
            state = BoardState(
                isLoading = false,
                canCreateTask = true,
                isFilteredToPending = true,
                // pendingHandoffs points at a task the board hasn't loaded — an edge case, but the
                // one that actually exercises an empty filtered view rather than showing a card.
                pendingHandoffs = listOf(
                    Handoff("h1", "stale-task", "u2", "Mei-Ling Chow", "u1", "You", null, HandoffStatus.OFFERED, null, 0, null)
                )
            ),
            onAction = {}, onNavigateToInbox = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {}
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
