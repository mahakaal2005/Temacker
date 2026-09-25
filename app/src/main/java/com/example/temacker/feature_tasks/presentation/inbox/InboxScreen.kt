package com.example.temacker.feature_tasks.presentation.inbox

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.example.temacker.core.presentation.components.AppDestination
import com.example.temacker.core.presentation.components.AppScaffold
import com.example.temacker.core.presentation.components.SwitcherPill
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun InboxRoot(
    onNavigateToBoard: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToYou: () -> Unit,
    onNavigateToIncoming: (String, String) -> Unit,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToSwitchProject: () -> Unit,
    viewModel: InboxViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is InboxEvent.NavigateToIncoming -> onNavigateToIncoming(event.taskId, event.handoffId)
            is InboxEvent.NavigateToTaskDetail -> onNavigateToTaskDetail(event.taskId)
        }
    }

    InboxScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToBoard = onNavigateToBoard,
        onNavigateToTeam = onNavigateToTeam,
        onNavigateToYou = onNavigateToYou,
        onNavigateToSwitchProject = onNavigateToSwitchProject
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    state: InboxState,
    onAction: (InboxAction) -> Unit,
    onNavigateToBoard: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToYou: () -> Unit,
    onNavigateToSwitchProject: () -> Unit
) {
    AppScaffold(
        selected = AppDestination.INBOX,
        onSelect = { destination ->
            when (destination) {
                AppDestination.BOARD -> onNavigateToBoard()
                AppDestination.INBOX -> Unit
                AppDestination.TEAM -> onNavigateToTeam()
                AppDestination.YOU -> onNavigateToYou()
            }
        },
        header = state.projectSummary?.let { summary ->
            {
                SwitcherPill(
                    projectName = summary.name,
                    metaLine = "${summary.memberCount} members · ${summary.roleName}",
                    hasOtherProjects = summary.hasOtherProjects,
                    onClick = onNavigateToSwitchProject
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            TopAppBar(
                title = { Text("Inbox") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

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
                    TextButton(onClick = { onAction(InboxAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            when {
                state.isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                state.waiting.isEmpty() && state.earlier.isEmpty() && state.otherProjects.isEmpty() -> InboxEmptyState()

                else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                    if (state.waiting.isNotEmpty()) {
                        item(key = "waiting-header") { SectionHeader("Waiting on you · ${state.waiting.size}") }
                        items(state.waiting, key = { "w-${it.handoffId}" }) { row ->
                            InboxRow(row = row, isWaiting = true, onClick = { onAction(InboxAction.OnRowClick(row)) })
                        }
                    }
                    state.otherProjects.forEach { project ->
                        item(key = "other-header-${project.projectId}") {
                            SectionHeader("Waiting in ${project.projectName} · ${project.rows.size}")
                        }
                        items(project.rows, key = { "o-${project.projectId}-${it.handoffId}" }) { row ->
                            InboxRow(
                                row = row,
                                isWaiting = true,
                                onClick = { onAction(InboxAction.OnOtherProjectRowClick(project.projectId, row)) }
                            )
                        }
                    }
                    if (state.earlier.isNotEmpty()) {
                        item(key = "earlier-header") { SectionHeader("Earlier") }
                        items(state.earlier, key = { "e-${it.handoffId}" }) { row ->
                            InboxRow(row = row, isWaiting = false, onClick = { onAction(InboxAction.OnRowClick(row)) })
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
        modifier = Modifier.padding(start = 6.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun InboxRow(row: InboxRowUi, isWaiting: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isWaiting) Amber else Line)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(row.title, style = MaterialTheme.typography.bodyLarge)
            Text(row.detail, style = MaterialTheme.typography.bodySmall, color = Ink500, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun InboxEmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nothing waiting on you", style = MaterialTheme.typography.headlineSmall)
        Text(
            "When someone hands you a task, it shows up here — and in a push if you've turned those on.",
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500,
            modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
        )
    }
}

private val previewWaiting = listOf(
    InboxRowUi("h1", "t1", "Daniel Osei is handing you Sponsor deck — final pass", "2h · offer open", InboxRowKind.OFFER_TO_YOU),
    InboxRowUi("h2", "t2", "Jonah Reed is handing you Press release draft", "5h · offer open", InboxRowKind.OFFER_TO_YOU)
)
private val previewEarlier = listOf(
    InboxRowUi("h3", "t3", "Aisha Bello still hasn't answered Stage plan sign-off", "18h unanswered · still yours", InboxRowKind.UNANSWERED),
    InboxRowUi("h4", "t4", "Mei-Ling Chow accepted Print vendor quotes", "Yesterday", InboxRowKind.ACCEPTED),
    InboxRowUi("h5", "t5", "Tomas Vidal declined Venue walkthrough", "Mon · \"I'm away until the 30th\"", InboxRowKind.DECLINED)
)

@Preview(showBackground = true)
@Composable
private fun InboxScreenLoadingPreview() {
    TemackerTheme {
        InboxScreen(state = InboxState(isLoading = true), onAction = {}, onNavigateToBoard = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InboxScreenEmptyPreview() {
    TemackerTheme {
        InboxScreen(state = InboxState(isLoading = false), onAction = {}, onNavigateToBoard = {}, onNavigateToTeam = {}, onNavigateToYou = {}, onNavigateToSwitchProject = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InboxScreenPopulatedPreview() {
    TemackerTheme {
        InboxScreen(
            state = InboxState(waiting = previewWaiting, earlier = previewEarlier, isLoading = false),
            onAction = {},
            onNavigateToBoard = {},
            onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InboxScreenOtherProjectsPreview() {
    TemackerTheme {
        InboxScreen(
            state = InboxState(
                waiting = previewWaiting,
                otherProjects = listOf(
                    OtherProjectUi(
                        "p2",
                        "Design Club",
                        listOf(InboxRowUi("h9", "t9", "Priya Nair is handing you Poster layout", "1h · offer open", InboxRowKind.OFFER_TO_YOU))
                    )
                ),
                earlier = previewEarlier,
                isLoading = false
            ),
            onAction = {},
            onNavigateToBoard = {},
            onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InboxScreenOnlyOtherProjectsPreview() {
    TemackerTheme {
        InboxScreen(
            state = InboxState(
                otherProjects = listOf(
                    OtherProjectUi(
                        "p2",
                        "Design Club",
                        listOf(InboxRowUi("h9", "t9", "Priya Nair is handing you Poster layout", "1h · offer open", InboxRowKind.OFFER_TO_YOU))
                    )
                ),
                isLoading = false
            ),
            onAction = {},
            onNavigateToBoard = {},
            onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InboxScreenEarlierOnlyPreview() {
    TemackerTheme {
        InboxScreen(
            state = InboxState(earlier = previewEarlier, isLoading = false),
            onAction = {},
            onNavigateToBoard = {},
            onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InboxScreenErrorPreview() {
    TemackerTheme {
        InboxScreen(
            state = InboxState(waiting = previewWaiting, isLoading = false, error = UiText.DynamicString("Couldn't sync. Showing what we have.")),
            onAction = {},
            onNavigateToBoard = {},
            onNavigateToTeam = {},
            onNavigateToYou = {},
            onNavigateToSwitchProject = {}
        )
    }
}
