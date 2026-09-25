package com.example.temacker.feature_profile.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.temacker.core.presentation.components.SwitcherPill
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Coral
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileRoot(
    onNavigateToBoard: () -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProjectGate: () -> Unit,
    onNavigateToSwitchProject: () -> Unit,
    onNavigateToYourData: () -> Unit,
    onNavigateToPlanLimits: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ProfileEvent.NavigateToLogin -> onNavigateToLogin()
            ProfileEvent.NavigateToProjectGate -> onNavigateToProjectGate()
        }
    }

    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToBoard = onNavigateToBoard,
        onNavigateToInbox = onNavigateToInbox,
        onNavigateToTeam = onNavigateToTeam,
        onNavigateToSwitchProject = onNavigateToSwitchProject,
        onNavigateToYourData = onNavigateToYourData,
        onNavigateToPlanLimits = onNavigateToPlanLimits
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onNavigateToBoard: () -> Unit,
    onNavigateToInbox: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToSwitchProject: () -> Unit,
    onNavigateToYourData: () -> Unit,
    onNavigateToPlanLimits: () -> Unit
) {
    AppScaffold(
        selected = AppDestination.YOU,
        onSelect = { destination ->
            when (destination) {
                AppDestination.BOARD -> onNavigateToBoard()
                AppDestination.INBOX -> onNavigateToInbox()
                AppDestination.TEAM -> onNavigateToTeam()
                AppDestination.YOU -> Unit
            }
        },
        header = {
            SwitcherPill(
                projectName = state.projectName,
                metaLine = "${state.memberCount} members · ${state.roleName.ifBlank { "Member" }}",
                hasOtherProjects = state.hasOtherProjects,
                onClick = onNavigateToSwitchProject
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Profile") },
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
                        TextButton(onClick = { onAction(ProfileAction.OnErrorDismissed) }) {
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

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Column {
                                SettingsRow(label = "Your data", onClick = onNavigateToYourData)
                                HorizontalDivider(color = Line)
                                SettingsRow(label = "Plan & limits", onClick = onNavigateToPlanLimits)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (state.projectName.isNotBlank()) {
                            if (state.isLeader) {
                                Text(
                                    text = "You lead ${state.projectName}. Make another member Leader from the Team tab before you can leave.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Ink500,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                                )
                            } else {
                                OutlinedButton(
                                    onClick = { onAction(ProfileAction.OnLeaveProjectClick) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Coral),
                                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                                ) {
                                    Text("Leave ${state.projectName}")
                                }
                            }
                        }

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

    if (state.isLeaveDialogVisible) {
        AlertDialog(
            onDismissRequest = { onAction(ProfileAction.OnLeaveProjectDismissed) },
            title = { Text("Leave ${state.projectName}?") },
            text = {
                Text(
                    "You'll lose access to its board and inbox. Tasks you hold go back to the Leader, " +
                        "and offers involving you are closed. You'll need a new invite code to come back."
                )
            },
            confirmButton = {
                TextButton(onClick = { onAction(ProfileAction.OnLeaveProjectConfirmed) }) {
                    Text("Leave", color = Coral)
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ProfileAction.OnLeaveProjectDismissed) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink900)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Ink500)
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
                isLeader = true,
                memberSince = "August 2026",
                isLoading = false
            ),
            onAction = {},
            onNavigateToBoard = {}, onNavigateToInbox = {},
            onNavigateToTeam = {},
            onNavigateToSwitchProject = {},
            onNavigateToYourData = {},
            onNavigateToPlanLimits = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenMemberPreview() {
    TemackerTheme {
        ProfileScreen(
            state = ProfileState(
                displayName = "Daniel Osei",
                email = "daniel.osei@northwind.co",
                projectName = "Aurora Launch",
                roleName = "Editor",
                memberSince = "August 2026",
                isLoading = false
            ),
            onAction = {},
            onNavigateToBoard = {}, onNavigateToInbox = {},
            onNavigateToTeam = {},
            onNavigateToSwitchProject = {},
            onNavigateToYourData = {},
            onNavigateToPlanLimits = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLeaveDialogPreview() {
    TemackerTheme {
        ProfileScreen(
            state = ProfileState(
                displayName = "Daniel Osei",
                email = "daniel.osei@northwind.co",
                projectName = "Aurora Launch",
                roleName = "Editor",
                memberSince = "August 2026",
                isLoading = false,
                isLeaveDialogVisible = true
            ),
            onAction = {},
            onNavigateToBoard = {}, onNavigateToInbox = {},
            onNavigateToTeam = {},
            onNavigateToSwitchProject = {},
            onNavigateToYourData = {},
            onNavigateToPlanLimits = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLoadingPreview() {
    TemackerTheme {
        ProfileScreen(
            state = ProfileState(isLoading = true), onAction = {}, onNavigateToBoard = {}, onNavigateToInbox = {},
            onNavigateToTeam = {}, onNavigateToSwitchProject = {}, onNavigateToYourData = {}, onNavigateToPlanLimits = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenErrorPreview() {
    TemackerTheme {
        ProfileScreen(
            state = ProfileState(
                displayName = "Priya Raman",
                email = "priya.raman@northwind.co",
                projectName = "Aurora Launch",
                roleName = "Leader",
                memberSince = "August 2026",
                isLoading = false,
                error = UiText.DynamicString("Couldn't sync with the server. Showing your last known data.")
            ),
            onAction = {},
            onNavigateToBoard = {}, onNavigateToInbox = {},
            onNavigateToTeam = {},
            onNavigateToSwitchProject = {},
            onNavigateToYourData = {},
            onNavigateToPlanLimits = {}
        )
    }
}
