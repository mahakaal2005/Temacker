package com.example.temacker.feature_project.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.AppDestination
import com.example.temacker.core.presentation.components.AppScaffold
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoot(
    onNavigateToHome: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HomeEvent.NavigateToRoster -> onNavigateToRoster()
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToHome = onNavigateToHome,
        onNavigateToRoster = onNavigateToRoster,
        onNavigateToProfile = onNavigateToProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    AppScaffold(
        selected = AppDestination.HOME,
        onSelect = { destination ->
            when (destination) {
                AppDestination.HOME -> onNavigateToHome()
                AppDestination.ROSTER -> onNavigateToRoster()
                AppDestination.PROFILE -> onNavigateToProfile()
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Column {
                            Text(state.projectName.ifBlank { "…" }, style = MaterialTheme.typography.titleMedium)
                            val roleText = if (state.isLeader) "You lead" else "Member"
                            Text(
                                "${state.memberCount} members · $roleText",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink500
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )

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
                        TextButton(onClick = { onAction(HomeAction.OnErrorDismissed) }) {
                            Text("Dismiss")
                        }
                    }
                }

                if (state.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COMING NEXT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink500
                        )
                        Text(
                            text = "Task board arrives in Phase 2",
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink900,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Text(
                            text = "Your roster and roles are live now. Set those up and the board will drop straight into this space.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Ink500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 11.dp)
                        )
                        TextButton(
                            onClick = { onAction(HomeAction.OnGoToRosterClick) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Go to roster →")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TemackerTheme {
        HomeScreen(
            state = HomeState(projectName = "Aurora Launch", memberCount = 8, isLeader = true, isLoading = false),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    TemackerTheme {
        HomeScreen(
            state = HomeState(isLoading = true),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    TemackerTheme {
        HomeScreen(
            state = HomeState(
                projectName = "Aurora Launch",
                memberCount = 8,
                isLeader = true,
                isLoading = false,
                error = UiText.DynamicString("Couldn't sync with the server. Showing your last known data.")
            ),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {},
            onNavigateToProfile = {}
        )
    }
}
