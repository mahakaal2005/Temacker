package com.example.temacker.feature_tasks.presentation.handoff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.core.presentation.components.Avatar
import com.example.temacker.core.presentation.components.EmptyState
import com.example.temacker.core.presentation.components.ListRow
import com.example.temacker.core.presentation.components.LoadingState
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HandoffRoot(
    taskId: String,
    onNavigateBack: () -> Unit,
    viewModel: HandoffViewModel = koinViewModel(parameters = { parametersOf(taskId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HandoffEvent.NavigateBack -> onNavigateBack()
        }
    }

    HandoffScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

// The spec calls for Hand off as a modal bottom sheet ("you're still looking at the task"), which
// would need a real overlay/dialog nav destination — a navigation-graph change out of this
// phase's presentation-only scope (CLAUDE.md rule 2, flagged rather than done silently). This
// keeps the existing full-screen destination and chrome, updating only its content to the shared
// components (ListRow, Avatar, EmptyState, TmkButton).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffScreen(state: HandoffState, onAction: (HandoffAction) -> Unit, onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(state.task?.title ?: "Hand off") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(HandoffAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            Text(
                "They keep the baton only once they accept. Until then it stays with you.",
                style = MaterialTheme.typography.bodySmall,
                color = Ink500,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xs)
            )

            when {
                state.isLoading -> LoadingState()
                state.members.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.Groups,
                    title = "No one to hand this to yet",
                    body = "Invite a teammate first, so there's someone to accept it.",
                    primaryLabel = "Back to task",
                    onPrimaryClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                )
                else -> {
                    LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = Spacing.s)) {
                        items(state.members, key = { it.uid }) { member ->
                            MemberPickRow(
                                member = member,
                                isSelected = state.selectedUid == member.uid,
                                onClick = { onAction(HandoffAction.OnMemberSelected(member.uid)) }
                            )
                            HorizontalDivider(color = Line)
                        }
                    }
                    Column(modifier = Modifier.imePadding().navigationBarsPadding()) {
                        OutlinedTextField(
                            value = state.note,
                            onValueChange = { onAction(HandoffAction.OnNoteChange(it)) },
                            label = { Text("Note") },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l).height(90.dp)
                        )
                        TmkButton(
                            text = "Send handoff",
                            onClick = { onAction(HandoffAction.OnSendClick) },
                            enabled = state.selectedUid != null,
                            isLoading = state.isSending,
                            icon = Icons.Rounded.Check,
                            modifier = Modifier.padding(Spacing.l)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberPickRow(member: ProjectMember, isSelected: Boolean, onClick: () -> Unit) {
    ListRow(
        headline = member.displayName,
        supporting = member.roleName,
        leading = { Avatar(name = member.displayName) },
        trailing = { RadioButton(selected = isSelected, onClick = onClick) },
        onClick = onClick
    )
}

@Preview(showBackground = true)
@Composable
private fun HandoffScreenPreview() {
    TemackerTheme {
        HandoffScreen(
            state = HandoffState(
                members = listOf(
                    ProjectMember("u2", "Tomas Vidal", null, "Scheduler", canAssignTasks = false, canEditAnyTask = false),
                    ProjectMember("u3", "Aisha Bello", null, "Default", canAssignTasks = false, canEditAnyTask = false)
                ),
                selectedUid = "u2"
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HandoffScreenLoadingPreview() {
    TemackerTheme {
        HandoffScreen(state = HandoffState(isLoading = true), onAction = {}, onNavigateBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HandoffScreenEmptyPreview() {
    TemackerTheme {
        HandoffScreen(state = HandoffState(isLoading = false, members = emptyList()), onAction = {}, onNavigateBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HandoffScreenSendingPreview() {
    TemackerTheme {
        HandoffScreen(
            state = HandoffState(
                members = listOf(ProjectMember("u2", "Tomas Vidal", null, "Scheduler", canAssignTasks = false, canEditAnyTask = false)),
                selectedUid = "u2",
                isSending = true
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
