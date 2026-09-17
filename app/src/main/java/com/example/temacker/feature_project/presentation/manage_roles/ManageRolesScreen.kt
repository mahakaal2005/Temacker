package com.example.temacker.feature_project.presentation.manage_roles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions
import org.koin.androidx.compose.koinViewModel

@Composable
fun ManageRolesRoot(
    onNavigateBack: () -> Unit,
    viewModel: ManageRolesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ManageRolesEvent.NavigateBack -> onNavigateBack()
        }
    }

    ManageRolesScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRolesScreen(state: ManageRolesState, onAction: (ManageRolesAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Manage roles") },
                navigationIcon = {
                    IconButton(onClick = { onAction(ManageRolesAction.OnBackClick) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(ManageRolesAction.OnAddRoleClick) }) {
                        Icon(Icons.Default.Add, contentDescription = "New role")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onAction(ManageRolesAction.OnErrorDismissed) }) {
                        Text("Dismiss")
                    }
                }
            }

            if (state.isLoading) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(state.roles, key = { it.id }) { role ->
                        if (role.isLeader) {
                            LeaderRoleCard(role)
                        } else {
                            EditableRoleCard(
                                role = role,
                                onPermissionToggle = { permissions -> onAction(ManageRolesAction.OnPermissionToggle(role.id, permissions)) },
                                onDeleteClick = { onAction(ManageRolesAction.OnDeleteRoleClick(role.id)) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.isCreateDialogVisible) {
        AlertDialog(
            onDismissRequest = { onAction(ManageRolesAction.OnDismissCreateDialog) },
            title = { Text("New role") },
            text = {
                OutlinedTextField(
                    value = state.newRoleName,
                    onValueChange = { onAction(ManageRolesAction.OnNewRoleNameChange(it)) },
                    label = { Text("Role name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { onAction(ManageRolesAction.OnCreateRoleConfirm) }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ManageRolesAction.OnDismissCreateDialog) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LeaderRoleCard(role: Role) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AmberWash),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AmberInk, modifier = Modifier.padding(end = 10.dp))
                Text(role.name, style = MaterialTheme.typography.titleMedium, color = AmberInk)
            }
            Text(
                "Full access to this project. Granted at creation and not transferable — so it can't be edited or removed here.",
                style = MaterialTheme.typography.bodyMedium,
                color = AmberInk,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun EditableRoleCard(
    role: Role,
    onPermissionToggle: (RolePermissions) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(role.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onDeleteClick) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            }

            Text("TEAM", style = MaterialTheme.typography.labelSmall, color = Ink500, modifier = Modifier.padding(top = 10.dp, bottom = 4.dp))
            PermissionRow("Invite members", "Can generate invite codes", role.permissions.manageInviteCode) {
                onPermissionToggle(role.permissions.copy(manageInviteCode = it))
            }
            PermissionRow("Reassign roles", null, role.permissions.manageRoles) {
                onPermissionToggle(role.permissions.copy(manageRoles = it))
            }
            PermissionRow("Remove members", null, role.permissions.removeMembers) {
                onPermissionToggle(role.permissions.copy(removeMembers = it))
            }
            PermissionRow("Delete project", "Permanent — cannot be undone", role.permissions.deleteProject) {
                onPermissionToggle(role.permissions.copy(deleteProject = it))
            }

            Text("TASKS", style = MaterialTheme.typography.labelSmall, color = Ink500, modifier = Modifier.padding(top = 14.dp, bottom = 4.dp))
            PermissionRow("Assign tasks", null, role.permissions.assignTasks) {
                onPermissionToggle(role.permissions.copy(assignTasks = it))
            }
            PermissionRow("Edit any task", null, role.permissions.editAnyTask) {
                onPermissionToggle(role.permissions.copy(editAnyTask = it))
            }
            PermissionRow("Manage tags", null, role.permissions.manageTags) {
                onPermissionToggle(role.permissions.copy(manageTags = it))
            }
        }
    }
}

@Composable
private fun PermissionRow(name: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink500) }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageRolesScreenPreview() {
    TemackerTheme {
        ManageRolesScreen(
            state = ManageRolesState(
                isLoading = false,
                roles = listOf(
                    Role("r1", "p1", "Leader", RolePermissions.ALL_GRANTED, isLeader = true),
                    Role("r2", "p1", "Editor", RolePermissions(manageInviteCode = true, assignTasks = true), isLeader = false),
                    Role("r3", "p1", "Default", RolePermissions.NONE, isLeader = false)
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageRolesScreenLoadingPreview() {
    TemackerTheme {
        ManageRolesScreen(state = ManageRolesState(isLoading = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageRolesScreenEmptyPreview() {
    TemackerTheme {
        ManageRolesScreen(state = ManageRolesState(isLoading = false, roles = emptyList()), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageRolesScreenCreateDialogPreview() {
    TemackerTheme {
        ManageRolesScreen(
            state = ManageRolesState(isLoading = false, isCreateDialogVisible = true, newRoleName = "Scheduler"),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ManageRolesScreenErrorPreview() {
    TemackerTheme {
        ManageRolesScreen(
            state = ManageRolesState(
                isLoading = false,
                roles = listOf(Role("r1", "p1", "Leader", RolePermissions.ALL_GRANTED, isLeader = true)),
                error = UiText.DynamicString("Couldn't update role. Check your connection and try again.")
            ),
            onAction = {}
        )
    }
}
