package com.example.temacker.feature_profile.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.AppDestination
import com.example.temacker.core.presentation.components.AppScaffold
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Coral
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileRoot(
    onNavigateToHome: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ProfileEvent.NavigateToLogin -> onNavigateToLogin()
        }
    }

    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToHome = onNavigateToHome,
        onNavigateToRoster = onNavigateToRoster
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToRoster: () -> Unit
) {
    AppScaffold(
        selected = AppDestination.PROFILE,
        onSelect = { destination ->
            when (destination) {
                AppDestination.HOME -> onNavigateToHome()
                AppDestination.ROSTER -> onNavigateToRoster()
                AppDestination.PROFILE -> Unit
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Profile") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )

                if (state.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(shape = CircleShape, color = TealWash, modifier = Modifier.size(72.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        state.displayName.take(2).uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TealInk
                                    )
                                }
                            }
                            Text(
                                text = state.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Ink900,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 14.dp)
                            )
                            Text(
                                text = state.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ink500,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ProfileRow(label = "Current project", value = state.projectName.ifBlank { "—" })
                                if (state.roleName == "Leader") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Your role", style = MaterialTheme.typography.bodyMedium, color = Ink500)
                                        Surface(color = AmberWash, shape = MaterialTheme.shapes.small) {
                                            Text(
                                                text = state.roleName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = AmberInk,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                } else {
                                    ProfileRow(label = "Your role", value = state.roleName.ifBlank { "—" })
                                }
                                ProfileRow(label = "Member since", value = state.memberSince.ifBlank { "—" })
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        OutlinedButton(
                            onClick = { onAction(ProfileAction.OnSignOutClick) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Coral),
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                        ) {
                            Text("Sign out")
                        }
                        Text(
                            text = "You'll need to sign in again to get back in.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink500)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Ink900)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    TemackerTheme {
        ProfileScreen(
            state = ProfileState(
                displayName = "Priya Raman",
                email = "priya.raman@northwind.co",
                projectName = "Aurora Launch",
                roleName = "Leader",
                memberSince = "August 2026",
                isLoading = false
            ),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLoadingPreview() {
    TemackerTheme {
        ProfileScreen(state = ProfileState(isLoading = true), onAction = {}, onNavigateToHome = {}, onNavigateToRoster = {})
    }
}
