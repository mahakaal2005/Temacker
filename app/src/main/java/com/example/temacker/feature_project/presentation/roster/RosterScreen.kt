package com.example.temacker.feature_project.presentation.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.AppDestination
import com.example.temacker.core.presentation.components.AppScaffold
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions
import org.koin.androidx.compose.koinViewModel

@Composable
fun RosterRoot(
    onNavigateToHome: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToManageRoles: () -> Unit,
    viewModel: RosterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            RosterEvent.NavigateToManageRoles -> onNavigateToManageRoles()
        }
    }

    RosterScreen(
        state = state,
        onAction = { action ->
            if (action is RosterAction.OnCopyCodeClick) {
                state.inviteCode?.let { clipboard.setText(AnnotatedString(it)) }
            }
            viewModel.onAction(action)
        },
        onNavigateToHome = onNavigateToHome,
        onNavigateToRoster = onNavigateToRoster,
        onNavigateToProfile = onNavigateToProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterScreen(
    state: RosterState,
    onAction: (RosterAction) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    AppScaffold(
        selected = AppDestination.ROSTER,
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
                    title = { Text("Roster") },
                    actions = {
                        if (state.canManageInvite) {
                            IconButton(onClick = { onAction(RosterAction.OnInviteClick) }) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Invite member")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${state.members.size} members", style = MaterialTheme.typography.labelSmall, color = Ink500)
                    if (state.canManageRoles) {
                        TextButton(onClick = { onAction(RosterAction.OnManageRolesClick) }, modifier = Modifier.padding(start = 0.dp)) {
                            Text("Manage roles →")
                        }
                    }
                }

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
                        TextButton(onClick = { onAction(RosterAction.OnErrorDismissed) }) {
                            Text("Dismiss")
                        }
                    }
                }

                if (state.isLoading) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        items(state.members, key = { it.userId }) { member ->
                            MemberRow(
                                member = member,
                                canAct = state.canRemoveMembers || state.canManageRoles,
                                isMenuOpen = state.menuForUserId == member.userId,
                                canReassign = state.canManageRoles,
                                canRemove = state.canRemoveMembers,
                                onMoreClick = { onAction(RosterAction.OnMemberMoreClick(member.userId)) },
                                onDismissMenu = { onAction(RosterAction.OnDismissMemberMenu) },
                                onReassignClick = { onAction(RosterAction.OnReassignRoleClick(member.userId)) },
                                onRemoveClick = { onAction(RosterAction.OnRemoveMemberClick(member.userId)) }
                            )
                            HorizontalDivider(color = Line)
                        }
                    }
                }
            }
        }
    }

    if (state.isInviteSheetVisible) {
        InviteCodeSheet(
            code = state.inviteCode,
            onDismiss = { onAction(RosterAction.OnDismissInviteSheet) },
            onCopy = { onAction(RosterAction.OnCopyCodeClick) },
            onGenerateNew = { onAction(RosterAction.OnGenerateNewCodeClick) }
        )
    }

    state.reassignTargetUserId?.let {
        ReassignRoleDialog(
            roles = state.roles,
            onDismiss = { onAction(RosterAction.OnDismissReassignSheet) },
            onRoleSelected = { roleId -> onAction(RosterAction.OnRoleSelected(roleId)) }
        )
    }
}

@Composable
private fun MemberRow(
    member: Membership,
    canAct: Boolean,
    isMenuOpen: Boolean,
    canReassign: Boolean,
    canRemove: Boolean,
    onMoreClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onReassignClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val isLeader = member.roleName == "Leader"
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = if (isLeader) AmberWash else TealWash, modifier = Modifier.size(40.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    member.displayName.take(2).uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLeader) AmberInk else TealInk
                )
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(member.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(member.roleName, style = MaterialTheme.typography.bodyMedium, color = if (isLeader) AmberInk else Ink500)
        }
        if (canAct && !isLeader) {
            Box {
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = isMenuOpen, onDismissRequest = onDismissMenu) {
                    if (canReassign) {
                        DropdownMenuItem(text = { Text("Reassign role") }, onClick = onReassignClick)
                    }
                    if (canRemove) {
                        DropdownMenuItem(
                            text = { Text("Remove from project", color = MaterialTheme.colorScheme.error) },
                            onClick = onRemoveClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InviteCodeSheet(
    code: String?,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onGenerateNew: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Text("Invite code", style = MaterialTheme.typography.titleLarge)
            Text(
                "Share this with anyone you want to join the project.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500,
                modifier = Modifier.padding(top = 6.dp)
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
                Text(
                    text = code ?: "Generating…",
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            Button(
                onClick = onCopy,
                enabled = code != null,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Copy code")
            }
            OutlinedButton(onClick = onGenerateNew, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text("Generate new code")
            }
            Text(
                "Codes stop working 7 days after they're created.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
    }
}

@Composable
private fun ReassignRoleDialog(
    roles: List<Role>,
    onDismiss: () -> Unit,
    onRoleSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Reassign role") },
        text = {
            Column {
                roles.filterNot { it.isLeader }.forEach { role ->
                    Text(
                        text = role.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoleSelected(role.id) }
                            .padding(vertical = 12.dp)
                    )
                    HorizontalDivider(color = Line)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun RosterScreenPreview() {
    TemackerTheme {
        RosterScreen(
            state = RosterState(
                isLoading = false,
                canManageInvite = true,
                canRemoveMembers = true,
                canManageRoles = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0),
                    Membership("p1", "u2", "r2", "Editor", RolePermissions(), "Daniel Osei", null, 0)
                ),
                roles = listOf(
                    Role("r1", "p1", "Leader", RolePermissions.ALL_GRANTED, isLeader = true),
                    Role("r2", "p1", "Editor", RolePermissions(), isLeader = false)
                )
            ),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterScreenLoadingPreview() {
    TemackerTheme {
        RosterScreen(
            state = RosterState(isLoading = true),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterScreenInviteSheetPreview() {
    TemackerTheme {
        RosterScreen(
            state = RosterState(
                isLoading = false,
                canManageInvite = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0)
                ),
                isInviteSheetVisible = true,
                inviteCode = "AB12CD34"
            ),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterScreenReassignDialogPreview() {
    TemackerTheme {
        RosterScreen(
            state = RosterState(
                isLoading = false,
                canManageRoles = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0),
                    Membership("p1", "u2", "r2", "Editor", RolePermissions(), "Daniel Osei", null, 0)
                ),
                roles = listOf(
                    Role("r1", "p1", "Leader", RolePermissions.ALL_GRANTED, isLeader = true),
                    Role("r2", "p1", "Editor", RolePermissions(), isLeader = false)
                ),
                reassignTargetUserId = "u2"
            ),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterScreenErrorPreview() {
    TemackerTheme {
        RosterScreen(
            state = RosterState(
                isLoading = false,
                canRemoveMembers = true,
                canManageRoles = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0)
                ),
                error = UiText.DynamicString("Couldn't remove member. Check your connection and try again.")
            ),
            onAction = {},
            onNavigateToHome = {},
            onNavigateToRoster = {},
            onNavigateToProfile = {}
        )
    }
}
