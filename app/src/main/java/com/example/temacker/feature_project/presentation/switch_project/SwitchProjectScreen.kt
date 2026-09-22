package com.example.temacker.feature_project.presentation.switch_project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun SwitchProjectRoot(
    onNavigateBack: () -> Unit,
    viewModel: SwitchProjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SwitchProjectEvent.NavigateBack -> onNavigateBack()
        }
    }

    SwitchProjectScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchProjectScreen(
    state: SwitchProjectState,
    onAction: (SwitchProjectAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Switch project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingBody(padding)
            state.error != null -> ErrorBody(padding, state.error.asString())
            else -> ProjectList(padding, state.rows, onAction)
        }
    }
}

@Composable
private fun LoadingBody(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp))
    }
}

@Composable
private fun ErrorBody(padding: PaddingValues, message: String) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ProjectList(padding: PaddingValues, rows: List<SwitchProjectRow>, onAction: (SwitchProjectAction) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
        items(rows, key = { it.projectId }) { row ->
            ProjectRow(row, onClick = { onAction(SwitchProjectAction.OnProjectClick(row.projectId)) })
        }
    }
}

@Composable
private fun ProjectRow(row: SwitchProjectRow, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(row.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${row.memberCount} members · ${row.roleName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (row.isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Current project", tint = TealInk)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchProjectScreenPreview() {
    TemackerTheme {
        SwitchProjectScreen(
            state = SwitchProjectState(
                isLoading = false,
                rows = listOf(
                    SwitchProjectRow("p1", "Aurora Launch", 8, "Leader", isSelected = true),
                    SwitchProjectRow("p2", "Cycle 2", 5, "Default", isSelected = false)
                )
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchProjectScreenLoadingPreview() {
    TemackerTheme {
        SwitchProjectScreen(
            state = SwitchProjectState(isLoading = true),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchProjectScreenErrorPreview() {
    TemackerTheme {
        SwitchProjectScreen(
            state = SwitchProjectState(
                isLoading = false,
                error = com.example.temacker.core.presentation.util.UiText.DynamicString("Couldn't load your projects.")
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
