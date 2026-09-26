package com.example.temacker.feature_project.presentation.succession

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.temacker.core.presentation.components.TmkButtonVariant
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun SuccessionRoot(
    onNavigateBack: () -> Unit,
    onNavigateToTeam: () -> Unit,
    viewModel: SuccessionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SuccessionEvent.NavigateBack -> onNavigateBack()
            SuccessionEvent.NavigateToTeam -> onNavigateToTeam()
        }
    }

    SuccessionScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessionScreen(state: SuccessionState, onAction: (SuccessionAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // imePadding keeps the Continue button above the keyboard.
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            TopAppBar(
                title = { Text("Start new cycle") },
                navigationIcon = {
                    IconButton(onClick = { onAction(SuccessionAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            StepperHeader(step = state.step)

            when (state.step) {
                SuccessionStep.NAME -> NameStep(state, onAction)
                SuccessionStep.CONFIRM -> ConfirmStep(state, onAction)
            }
        }
    }
}

@Composable
private fun StepperHeader(step: SuccessionStep) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = Spacing.s)
    ) {
        StepDot(number = 1, label = "Name", isActive = step == SuccessionStep.NAME, isDone = step == SuccessionStep.CONFIRM)
        Row(modifier = Modifier.weight(1f).padding(horizontal = Spacing.xs)) {
            HorizontalDivider(color = Ink500.copy(alpha = 0.3f))
        }
        StepDot(number = 2, label = "Confirm", isActive = step == SuccessionStep.CONFIRM, isDone = false)
    }
}

@Composable
private fun StepDot(number: Int, label: String, isActive: Boolean, isDone: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = if (isActive || isDone) MaterialTheme.colorScheme.primary else NeutralWash,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    "$number",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive || isDone) MaterialTheme.colorScheme.onPrimary else Ink500
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) MaterialTheme.colorScheme.onBackground else Ink500,
            modifier = Modifier.padding(start = Spacing.xxs)
        )
    }
}

@Composable
private fun ColumnScope.NameStep(state: SuccessionState, onAction: (SuccessionAction) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
        Text(
            text = "NEW CYCLE",
            style = MaterialTheme.typography.labelSmall,
            color = Ink500,
            modifier = Modifier.padding(vertical = 14.dp)
        )
        OutlinedTextField(
            value = state.newProjectName,
            onValueChange = { onAction(SuccessionAction.OnNameChange(it)) },
            label = { Text("New cycle name") },
            singleLine = true,
            supportingText = { Text("Everyone on the current roster carries over automatically.") },
            modifier = Modifier.fillMaxWidth()
        )

        state.error?.let { error ->
            Text(
                text = error.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        Button(
            onClick = { onAction(SuccessionAction.OnContinueClick) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun ColumnScope.ConfirmStep(state: SuccessionState, onAction: (SuccessionAction) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AmberWash),
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = AmberInk, modifier = Modifier.height(20.dp))
                Text(
                    text = "\"${state.newProjectName}\" will inherit the full current roster. " +
                        "The current project will be archived and can't be un-archived. This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberInk,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }

        state.error?.let { error ->
            Text(
                text = error.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        TmkButton(
            text = "Archive and start \"${state.newProjectName}\"",
            onClick = { onAction(SuccessionAction.OnConfirmClick) },
            variant = TmkButtonVariant.DESTRUCTIVE,
            isLoading = state.isSubmitting
        )
        TmkButton(
            text = "Back",
            onClick = { onAction(SuccessionAction.OnBackToNameClick) },
            variant = TmkButtonVariant.SECONDARY,
            enabled = !state.isSubmitting,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessionScreenNamePreview() {
    TemackerTheme {
        SuccessionScreen(state = SuccessionState(step = SuccessionStep.NAME), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessionScreenConfirmPreview() {
    TemackerTheme {
        SuccessionScreen(
            state = SuccessionState(newProjectName = "Hackathon Cycle 2", step = SuccessionStep.CONFIRM),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessionScreenSubmittingPreview() {
    TemackerTheme {
        SuccessionScreen(
            state = SuccessionState(newProjectName = "Hackathon Cycle 2", step = SuccessionStep.CONFIRM, isSubmitting = true),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessionScreenErrorPreview() {
    TemackerTheme {
        SuccessionScreen(
            state = SuccessionState(
                newProjectName = "Hackathon Cycle 2",
                step = SuccessionStep.CONFIRM,
                error = UiText.DynamicString("Couldn't start the new cycle. Check your connection and try again.")
            ),
            onAction = {}
        )
    }
}
