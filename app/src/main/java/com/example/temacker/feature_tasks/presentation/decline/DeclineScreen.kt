package com.example.temacker.feature_tasks.presentation.decline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeclineRoot(
    taskId: String,
    handoffId: String,
    onNavigateBack: () -> Unit,
    onNavigateToBoard: () -> Unit,
    viewModel: DeclineViewModel = koinViewModel(parameters = { parametersOf(taskId, handoffId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            DeclineEvent.NavigateBack -> onNavigateBack()
            DeclineEvent.NavigateToBoard -> onNavigateToBoard()
        }
    }

    DeclineScreen(state = state, onAction = viewModel::onAction)
}

// DeclineState has no task title to show "which task you're declining" — that needs a new State
// field (out of this presentation-only phase's scope, flagged rather than added silently).
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeclineScreen(state: DeclineState, onAction: (DeclineAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Decline handoff") },
                navigationIcon = {
                    IconButton(onClick = { onAction(DeclineAction.OnBackClick) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(DeclineAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(Spacing.l)) {
                OutlinedTextField(
                    value = state.reason,
                    onValueChange = { onAction(DeclineAction.OnReasonChange(it)) },
                    label = { Text("Why are you declining?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
                Text(
                    "They see this straight away, and keep the task.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink500,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
                FlowRow(modifier = Modifier.padding(top = Spacing.m)) {
                    DECLINE_REASON_CHIPS.forEach { chip ->
                        AssistChip(
                            onClick = { onAction(DeclineAction.OnQuickChipClick(chip)) },
                            label = { Text(chip) },
                            modifier = Modifier.padding(end = Spacing.xs, bottom = Spacing.xs)
                        )
                    }
                }
            }

            TmkButton(
                text = "Send decline",
                onClick = { onAction(DeclineAction.OnSendClick) },
                isLoading = state.isSending,
                modifier = Modifier.imePadding().navigationBarsPadding().padding(Spacing.l)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeclineScreenPreview() {
    TemackerTheme {
        DeclineScreen(state = DeclineState(reason = "I don't know which supplier we settled on"), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DeclineScreenSendingPreview() {
    TemackerTheme {
        DeclineScreen(state = DeclineState(reason = "Not my area", isSending = true), onAction = {})
    }
}
