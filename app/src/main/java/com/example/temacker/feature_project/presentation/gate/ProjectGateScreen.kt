package com.example.temacker.feature_project.presentation.gate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProjectGateRoot(
    onNavigateToHome: () -> Unit,
    onNavigateToNoProject: () -> Unit,
    viewModel: ProjectGateViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ProjectGateEvent.NavigateToHome -> onNavigateToHome()
            ProjectGateEvent.NavigateToNoProject -> onNavigateToNoProject()
        }
    }

    ProjectGateScreen()
}

@Composable
fun ProjectGateScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectGateScreenPreview() {
    TemackerTheme {
        ProjectGateScreen()
    }
}
