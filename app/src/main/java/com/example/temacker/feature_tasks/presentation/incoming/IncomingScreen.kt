package com.example.temacker.feature_tasks.presentation.incoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
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
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.NeutralWash
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
fun IncomingRoot(
    taskId: String,
    handoffId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDecline: (String, String) -> Unit,
    viewModel: IncomingViewModel = koinViewModel(parameters = { parametersOf(taskId, handoffId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            IncomingEvent.NavigateBack -> onNavigateBack()
            is IncomingEvent.NavigateToDecline -> onNavigateToDecline(event.taskId, event.handoffId)
        }
    }

    IncomingScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingScreen(state: IncomingState, onAction: (IncomingAction) -> Unit, onNavigateBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Handoff to you") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(IncomingAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            if (state.isLoading || state.task == null || state.handoff == null) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val task = state.task
                val handoff = state.handoff
                Column(modifier = Modifier.weight(1f).padding(18.dp)) {
                    Surface(shape = CircleShape, color = TealWash, modifier = Modifier.size(56.dp)) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(handoff.fromDisplayName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = TealInk)
                        }
                    }
                    Text(
                        "${handoff.fromDisplayName} wants to hand you \"${task.title}\"",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    handoff.note?.let {
                        Text("\"$it\"", style = MaterialTheme.typography.bodyMedium, color = Ink500, modifier = Modifier.padding(top = 12.dp))
                    }
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), colors = CardDefaults.cardColors(containerColor = NeutralWash)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Times handed over", color = Ink500)
                                Text("${task.timesHandedOver}")
                            }
                        }
                    }
                    Text(
                        "Until you accept, this task stays with ${handoff.fromDisplayName} and still counts as theirs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink500,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Button(
                        onClick = { onAction(IncomingAction.OnAcceptClick) },
                        enabled = !state.isResponding,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Accept the baton")
                    }
                    OutlinedButton(
                        onClick = { onAction(IncomingAction.OnDeclineClick) },
                        enabled = !state.isResponding,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Decline")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomingScreenPreview() {
    TemackerTheme {
        IncomingScreen(
            state = IncomingState(
                isLoading = false,
                task = Task("t1", "p1", "Sponsor deck — final pass", null, "u2", "Daniel Osei", TaskStatus.DOING, null, 3, "u1", "You", 0, 0),
                handoff = Handoff("h1", "t1", "u2", "Daniel Osei", "u1", "You", "Design's done — needs a print quote before Friday.", HandoffStatus.OFFERED, null, 0, null)
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomingScreenLoadingPreview() {
    TemackerTheme {
        IncomingScreen(state = IncomingState(isLoading = true), onAction = {}, onNavigateBack = {})
    }
}
