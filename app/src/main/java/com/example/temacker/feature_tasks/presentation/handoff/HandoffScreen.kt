package com.example.temacker.feature_tasks.presentation.handoff

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffScreen(state: HandoffState, onAction: (HandoffAction) -> Unit, onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(state.task?.title ?: "Hand off") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(HandoffAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            Text(
                "They keep the baton only once they accept. Until then it stays with you.",
                style = MaterialTheme.typography.bodySmall,
                color = Ink500,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)
            )

            if (state.isLoading) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    items(state.members, key = { it.uid }) { member ->
                        MemberPickRow(
                            member = member,
                            isSelected = state.selectedUid == member.uid,
                            onClick = { onAction(HandoffAction.OnMemberSelected(member.uid)) }
                        )
                        HorizontalDivider(color = Line)
                    }
                }
                OutlinedTextField(
                    value = state.note,
                    onValueChange = { onAction(HandoffAction.OnNoteChange(it)) },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(90.dp)
                )
                Button(
                    onClick = { onAction(HandoffAction.OnSendClick) },
                    enabled = state.selectedUid != null && !state.isSending,
                    modifier = Modifier.fillMaxWidth().padding(18.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Send handoff")
                }
            }
        }
    }
}

@Composable
private fun MemberPickRow(member: ProjectMember, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = TealWash, modifier = Modifier.size(40.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(member.displayName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = TealInk)
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(member.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(member.roleName, style = MaterialTheme.typography.bodySmall, color = Ink500)
        }
        RadioButton(selected = isSelected, onClick = onClick)
    }
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
