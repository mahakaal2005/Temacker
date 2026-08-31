package com.example.temacker.feature_project.presentation.join_project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun JoinProjectRoot(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: JoinProjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            JoinProjectEvent.NavigateBack -> onNavigateBack()
            JoinProjectEvent.NavigateToHome -> onNavigateToHome()
        }
    }

    JoinProjectScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinProjectScreen(state: JoinProjectState, onAction: (JoinProjectAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Join project") },
                navigationIcon = {
                    IconButton(onClick = { onAction(JoinProjectAction.OnBackClick) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp).weight(1f)) {
                Text(
                    text = "INVITE CODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
                OutlinedTextField(
                    value = state.code,
                    onValueChange = { onAction(JoinProjectAction.OnCodeChange(it)) },
                    label = { Text("Invite code") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    isError = state.error != null,
                    supportingText = state.error?.let { error -> { Text(error.asString(), color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Ink500, modifier = Modifier.height(20.dp))
                    Text(
                        text = "Codes look like TMK-XXXX-XX and stop working 7 days after a Leader creates them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink500,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Button(
                    onClick = { onAction(JoinProjectAction.OnJoinClick) },
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
                        Text("Join project")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JoinProjectScreenDefaultPreview() {
    TemackerTheme {
        JoinProjectScreen(state = JoinProjectState(code = "TMK-4K8Z-QD"), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun JoinProjectScreenErrorPreview() {
    TemackerTheme {
        JoinProjectScreen(
            state = JoinProjectState(
                code = "TMK-4K8Z-QD",
                error = UiText.DynamicString("This code expired. Ask your project Leader for a new one.")
            ),
            onAction = {}
        )
    }
}
