package com.example.temacker.feature_project.presentation.load

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.domain.model.HolderLoad
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoadRoot(viewModel: LoadViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoadTabContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun LoadTabContent(
    state: LoadState,
    onAction: (LoadAction) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    TextButton(onClick = { onAction(LoadAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            when {
                state.isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                state.holderLoads.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nobody's holding a task yet.", style = MaterialTheme.typography.bodyMedium, color = Ink500)
                }
                else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
                    items(state.holderLoads, key = { it.holderUid }) { load ->
                        HolderLoadRow(load)
                        HorizontalDivider(color = Line)
                    }
                }
            }
        }
    }
}

@Composable
private fun HolderLoadRow(load: HolderLoad) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(load.holderDisplayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${load.todoCount} to do · ${load.doingCount} doing",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500
            )
        }
        Text(
            "${load.totalActive}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadTabContentPreview() {
    TemackerTheme {
        LoadTabContent(
            state = LoadState(
                isLoading = false,
                holderLoads = listOf(
                    HolderLoad("u1", "Priya Raman", todoCount = 2, doingCount = 1),
                    HolderLoad("u2", "Daniel Osei", todoCount = 0, doingCount = 3)
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadTabContentLoadingPreview() {
    TemackerTheme {
        LoadTabContent(state = LoadState(isLoading = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadTabContentEmptyPreview() {
    TemackerTheme {
        LoadTabContent(state = LoadState(isLoading = false, holderLoads = emptyList()), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadTabContentErrorPreview() {
    TemackerTheme {
        LoadTabContent(
            state = LoadState(
                isLoading = false,
                holderLoads = listOf(HolderLoad("u1", "Priya Raman", todoCount = 2, doingCount = 1)),
                error = UiText.DynamicString("Couldn't load team load. Check your connection and try again.")
            ),
            onAction = {}
        )
    }
}
