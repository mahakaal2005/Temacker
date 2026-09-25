package com.example.temacker.feature_tasks.presentation.new_task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewTaskRoot(
    onNavigateBack: () -> Unit,
    onNavigateToBoard: () -> Unit,
    viewModel: NewTaskViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NewTaskEvent.NavigateBack -> onNavigateBack()
            NewTaskEvent.NavigateToBoard -> onNavigateToBoard()
        }
    }

    NewTaskScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(state: NewTaskState, onAction: (NewTaskAction) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("New task") },
                navigationIcon = {
                    IconButton(onClick = { onAction(NewTaskAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(NewTaskAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.l).padding(top = Spacing.xs)) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onAction(NewTaskAction.OnTitleChange(it)) },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { onAction(NewTaskAction.OnDescriptionChange(it)) },
                    label = { Text("Details") },
                    placeholder = { Text("Optional. What \"done\" looks like.") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.s)
                )
                // Tapping anywhere on the field opens the picker, not just the trailing icon.
                Box(modifier = Modifier.padding(top = Spacing.s).clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = state.dueDate?.let { SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(it)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due") },
                        trailingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = "Pick due date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Surface(color = AmberWash, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(top = Spacing.l)) {
                    Row(modifier = Modifier.padding(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = AmberInk)
                        Text(
                            "You'll hold this task until you hand it off and someone accepts it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberInk,
                            modifier = Modifier.padding(start = Spacing.xs)
                        )
                    }
                }
            }

            TmkButton(
                text = "Create task",
                onClick = { onAction(NewTaskAction.OnCreateClick) },
                isLoading = state.isLoading,
                modifier = Modifier.imePadding().navigationBarsPadding().padding(Spacing.l)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onAction(NewTaskAction.OnDueDateSelected(datePickerState.selectedDateMillis))
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewTaskScreenPreview() {
    TemackerTheme {
        NewTaskScreen(state = NewTaskState(title = "Print vendor quotes"), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun NewTaskScreenLoadingPreview() {
    TemackerTheme {
        NewTaskScreen(state = NewTaskState(title = "Print vendor quotes", isLoading = true), onAction = {})
    }
}
