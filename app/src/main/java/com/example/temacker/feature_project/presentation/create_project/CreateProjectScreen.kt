package com.example.temacker.feature_project.presentation.create_project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateProjectRoot(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: CreateProjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CreateProjectEvent.NavigateBack -> onNavigateBack()
            CreateProjectEvent.NavigateToHome -> onNavigateToHome()
        }
    }

    CreateProjectScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(state: CreateProjectState, onAction: (CreateProjectAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Create project") },
                navigationIcon = {
                    IconButton(onClick = { onAction(CreateProjectAction.OnBackClick) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
                Text(
                    text = "PROJECT DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onAction(CreateProjectAction.OnNameChange(it)) },
                    label = { Text("Project name") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    supportingText = { Text("Everyone you invite will see this name.") },
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = AmberWash),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AmberInk, modifier = Modifier.height(20.dp))
                        Text(
                            text = "Creating this project makes you its Leader. You can hand it to another member later, but it can't be reassigned like other roles.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberInk,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Ink500, modifier = Modifier.height(20.dp))
                    Text(
                        text = "You'll get an invite code to share as soon as the project exists.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink500,
                        modifier = Modifier.padding(start = 12.dp)
                    )
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
                Button(
                    onClick = { onAction(CreateProjectAction.OnCreateClick) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Create project")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateProjectScreenDefaultPreview() {
    TemackerTheme {
        CreateProjectScreen(state = CreateProjectState(name = "Aurora Launch"), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateProjectScreenLoadingPreview() {
    TemackerTheme {
        CreateProjectScreen(state = CreateProjectState(name = "Aurora Launch", isLoading = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateProjectScreenErrorPreview() {
    TemackerTheme {
        CreateProjectScreen(
            state = CreateProjectState(error = UiText.DynamicString("Enter a project name.")),
            onAction = {}
        )
    }
}
