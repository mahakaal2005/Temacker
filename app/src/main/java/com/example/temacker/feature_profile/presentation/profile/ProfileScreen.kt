package com.example.temacker.feature_profile.presentation.profile

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.AppDestination
import com.example.temacker.core.presentation.components.AppScaffold
import com.example.temacker.core.presentation.components.Avatar
import com.example.temacker.core.presentation.components.AvatarTone
import com.example.temacker.core.presentation.components.ChipTone
import com.example.temacker.core.presentation.components.InsetGroup
import com.example.temacker.core.presentation.components.ListRow
import com.example.temacker.core.presentation.components.StatusChip
import com.example.temacker.core.presentation.components.SwitcherPill
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.components.TmkButtonVariant
import com.example.temacker.core.presentation.components.bottomBarContentPadding
import com.example.temacker.core.presentation.designsystem.Coral
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.Spacing
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
                projectName = state.projectName.ifBlank { null },
                metaLine = state.projectName.takeIf { it.isNotBlank() }?.let { "${state.memberCount} members · ${state.roleName.ifBlank { "Member" }}" },
                hasOtherProjects = state.hasOtherProjects,
                onClick = onNavigateToSwitchProject
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(bottomBarContentPadding(padding)), color = MaterialTheme.colorScheme.background) {
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
                    val context = LocalContext.current
                    // Scrollable — Sign out / Leave project must stay reachable at large font
                    // scale or in a short window, not just when the content happens to fit.
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Avatar(name = state.displayName, size = 56.dp, tone = AvatarTone.ACCENT)
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

                        Text(
                            "PROJECT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink500,
                            modifier = Modifier.padding(top = 24.dp, bottom = Spacing.xxs)
                        )
                        InsetGroup {
                            ListRow(
                                headline = "Current project",
                                supporting = state.projectName.ifBlank { "—" },
                                trailing = { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500) },
                                onClick = onNavigateToSwitchProject
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            ListRow(
                                headline = "Your role",
                                trailing = {
                                    StatusChip(
                                        text = state.roleName.ifBlank { "—" },
                                        tone = if (state.roleName == "Leader") ChipTone.WARNING else ChipTone.NEUTRAL
                                    )
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            ListRow(headline = "Member since", supporting = state.memberSince.ifBlank { "—" })
                        }

                        Text(
                            "SETTINGS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink500,
                            modifier = Modifier.padding(top = Spacing.l, bottom = Spacing.xxs)
                        )
                        InsetGroup {
                            ListRow(
                                headline = "Notifications",
                                leading = { Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Ink500) },
                                trailing = { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500) },
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    context.startActivity(intent)
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            ListRow(
                                headline = "Your data",
                                trailing = { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500) },
                                onClick = onNavigateToYourData
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            ListRow(
                                headline = "Plan & limits",
                                trailing = { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500) },
                                onClick = onNavigateToPlanLimits
                            )
                        }

                        // weight(1f) can't push content to the bottom inside a scrollable Column
                        // (unbounded height), so this is a fixed gap instead of a push-to-bottom spacer.
                        Spacer(modifier = Modifier.height(Spacing.xxl))

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
                                // Leave project stays the only coral (destructive) item on this screen.
                                TmkButton(
                                    text = "Leave ${state.projectName}",
                                    onClick = { onAction(ProfileAction.OnLeaveProjectClick) },
                                    variant = TmkButtonVariant.DESTRUCTIVE,
                                    modifier = Modifier.padding(top = 20.dp)
                                )
                            }
                        }

                        // Sign out is now a neutral text button — it isn't destructive the way leaving a project is.
                        TmkButton(
                            text = "Sign out",
                            onClick = { onAction(ProfileAction.OnSignOutClick) },
                            variant = TmkButtonVariant.TEXT,
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                        )
                        Text(
                            text = "You'll need to sign in again to get back in.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 18.dp)
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
