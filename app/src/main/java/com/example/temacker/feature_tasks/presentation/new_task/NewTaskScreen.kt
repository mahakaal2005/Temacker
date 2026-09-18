package com.example.temacker.feature_tasks.presentation.new_task

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.DatePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(NewTaskAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 18.dp).padding(top = 8.dp)) {
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
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(100.dp)
                )
                OutlinedTextField(
                    value = state.dueDate?.let { SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(it)) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Due") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick due date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberWash)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AmberInk)
                        Text(
                            "You'll hold this task until you hand it off and someone accepts it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberInk,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = { onAction(NewTaskAction.OnCreateClick) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create task")
                }
            }
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
