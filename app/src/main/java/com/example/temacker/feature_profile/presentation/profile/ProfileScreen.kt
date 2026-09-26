package com.example.temacker.feature_profile.presentation.profile

import com.example.temacker.core.presentation.components.SectionLabel
import com.example.temacker.core.presentation.components.IconBadge
import com.example.temacker.core.presentation.components.RowChevron
import com.example.temacker.core.presentation.components.InsetDivider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.CoralInk
import com.example.temacker.core.presentation.designsystem.CoralWash
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.White
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
import com.example.temacker.core.presentation.components.memberCountLabel
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
                metaLine = state.projectName.takeIf { it.isNotBlank() }?.let { "${memberCountLabel(state.memberCount)} · ${state.roleName.ifBlank { "Member" }}" },
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
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.m),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
                    ) {
                        ProfileCard(name = state.displayName, email = state.email)

                        if (state.projectName.isNotBlank()) {
                            SectionLabel("In ${state.projectName}")
                            InsetGroup {
                                ListRow(
                                    headline = "Your role",
                                    supporting = state.memberSince.takeIf { it.isNotBlank() }?.let { "Member since $it" },
                                    leading = { IconBadge(Icons.Rounded.Badge, AmberInk, AmberWash) },
                                    trailing = {
                                        StatusChip(
                                            text = state.roleName.ifBlank { "Member" },
                                            tone = if (state.isLeader) ChipTone.WARNING else ChipTone.NEUTRAL
                                        )
                                    }
                                )
                            }
                        }

                        SectionLabel("Settings")
                        InsetGroup {
                            ListRow(
                                headline = "Notifications",
                                supporting = "Handoff pushes and reminders",
                                leading = { IconBadge(Icons.Rounded.Notifications, AmberInk, AmberWash) },
                                trailing = { RowChevron() },
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    context.startActivity(intent)
                                }
                            )
                            InsetDivider()
                            ListRow(
                                headline = "Your data",
                                supporting = "Export or delete your account",
                                leading = { IconBadge(Icons.Rounded.Shield, TealInk, TealWash) },
                                trailing = { RowChevron() },
                                onClick = onNavigateToYourData
                            )
                            InsetDivider()
                            ListRow(
                                headline = "Plan & limits",
                                supporting = "What your project can hold",
                                leading = { IconBadge(Icons.Rounded.Speed, Ink500, NeutralWash) },
                                trailing = { RowChevron() },
                                onClick = onNavigateToPlanLimits
                            )
                        }

                        SectionLabel("Account")
                        InsetGroup {
                            if (state.projectName.isNotBlank()) {
                                if (state.isLeader) {
                                    // Disabled row, not hidden, so Leaders learn why they can't leave yet.
                                    ListRow(
                                        headline = "Leave ${state.projectName}",
                                        supporting = "Make another member Leader from the Team tab first",
                                        leading = { IconBadge(Icons.AutoMirrored.Rounded.ExitToApp, Ink500.copy(alpha = 0.5f), NeutralWash) },
                                        modifier = Modifier.alpha(0.6f)
                                    )
                                } else {
                                    ListRow(
                                        headline = "Leave ${state.projectName}",
                                        supporting = "Your tasks go back to the Leader",
                                        leading = { IconBadge(Icons.AutoMirrored.Rounded.ExitToApp, CoralInk, CoralWash) },
                                        onClick = { onAction(ProfileAction.OnLeaveProjectClick) }
                                    )
                                }
                                InsetDivider()
                            }
                            ListRow(
                                headline = "Sign out",
                                leading = { IconBadge(Icons.AutoMirrored.Rounded.Logout, Ink500, NeutralWash) },
                                onClick = { onAction(ProfileAction.OnSignOutClick) }
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.l))
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
private fun ProfileCard(name: String, email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xs)
            .background(White, RoundedCornerShape(20.dp))
            .border(1.dp, Line, RoundedCornerShape(20.dp))
            .padding(Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = name, size = 56.dp, tone = AvatarTone.ACCENT)
        Column(modifier = Modifier.weight(1f).padding(start = Spacing.m)) {
            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ink900, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(email, style = MaterialTheme.typography.bodyMedium, color = Ink500, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
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
