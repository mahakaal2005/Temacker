package com.example.temacker.feature_project.presentation.manage_roles

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.White
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.ui.text.font.FontWeight
import com.example.temacker.core.presentation.components.IconBadge
import com.example.temacker.core.presentation.components.InsetDivider
import com.example.temacker.core.presentation.components.SectionLabel
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.CoralInk
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
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
import com.example.temacker.core.presentation.components.TmkSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.InsetGroup
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.rememberAppHaptics
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
            Row(
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(start = 4.dp, end = Spacing.m, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAction(ManageRolesAction.OnBackClick) }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text("Manage roles", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Surface(onClick = { onAction(ManageRolesAction.OnAddRoleClick) }, shape = RoundedCornerShape(50), color = AmberWash) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = AmberInk, modifier = Modifier.size(18.dp))
                        Text("New role", style = MaterialTheme.typography.labelLarge, color = AmberInk, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

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
                // One role open at a time keeps ten roles scannable; a role you just created opens itself.
                var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
                var knownIds by remember { mutableStateOf<Set<String>?>(null) }
                LaunchedEffect(state.roles) {
                    val ids = state.roles.map { it.id }.toSet()
                    knownIds?.let { known -> (ids - known).firstOrNull()?.let { expandedId = it } }
                    knownIds = ids
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = Spacing.m, end = Spacing.m, bottom = Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.roles.sortedByDescending { it.isLeader }, key = { it.id }) { role ->
                        if (role.isLeader) {
                            LeaderRoleCard(role)
                        } else {
                            EditableRoleCard(
                                role = role,
                                expanded = expandedId == role.id,
                                onToggleExpanded = { expandedId = if (expandedId == role.id) null else role.id },
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
            title = { Text("New role", fontWeight = FontWeight.Bold) },
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
                TextButton(onClick = { onAction(ManageRolesAction.OnCreateRoleConfirm) }) { Text("Create", color = AmberInk) }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ManageRolesAction.OnDismissCreateDialog) }) { Text("Cancel", color = Ink500) }
            }
        )
    }
}

@Composable
private fun LeaderRoleCard(role: Role) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AmberWash, RoundedCornerShape(18.dp))
            .border(1.dp, Amber.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(Spacing.m),
        verticalAlignment = Alignment.Top
    ) {
        IconBadge(Icons.Rounded.Lock, AmberInk, Amber.copy(alpha = 0.25f))
        Column(modifier = Modifier.padding(start = Spacing.s)) {
            Text(role.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberInk)
            Text(
                "Full access, always. It can't be edited here. To hand it to someone else, use Make Leader on the Team tab.",
                style = MaterialTheme.typography.bodyMedium,
                color = AmberInk,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun EditableRoleCard(
    role: Role,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onPermissionToggle: (RolePermissions) -> Unit,
    onDeleteClick: () -> Unit
) {
    val p = role.permissions
    val enabledCount = listOf(p.manageInviteCode, p.manageRoles, p.removeMembers, p.deleteProject, p.assignTasks, p.editAnyTask, p.manageTags).count { it }
    val shape = RoundedCornerShape(18.dp)
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "roleChevron")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, shape)
            .border(1.dp, if (expanded) Amber else Line, shape)
            .clip(shape)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = if (expanded) "Collapse ${role.name}" else "Edit ${role.name}", onClick = onToggleExpanded)
                .padding(horizontal = Spacing.m, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(role.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (enabledCount == 0) "No permissions yet" else "$enabledCount of 7 permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink500
                )
            }
            Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = Ink500, modifier = Modifier.rotate(chevronRotation))
        }
        if (expanded) {
            HorizontalDivider(color = Line)
            Column(modifier = Modifier.padding(bottom = Spacing.xs)) {
                SectionLabel("Team", modifier = Modifier.padding(start = Spacing.m))
                PermissionRow("Invite members", "Create and share invite codes", p.manageInviteCode) { onPermissionToggle(p.copy(manageInviteCode = it)) }
                InsetDivider(start = Spacing.m)
                PermissionRow("Reassign roles", "Change which role a member has", p.manageRoles) { onPermissionToggle(p.copy(manageRoles = it)) }
                InsetDivider(start = Spacing.m)
                PermissionRow("Remove members", "Take someone out of the project", p.removeMembers) { onPermissionToggle(p.copy(removeMembers = it)) }
                InsetDivider(start = Spacing.m)
                PermissionRow("Delete project", "Permanent, can't be undone", p.deleteProject, isDangerous = true) { onPermissionToggle(p.copy(deleteProject = it)) }

                SectionLabel("Tasks", modifier = Modifier.padding(start = Spacing.m))
                PermissionRow("Assign tasks", "Give tasks to other members", p.assignTasks) { onPermissionToggle(p.copy(assignTasks = it)) }
                InsetDivider(start = Spacing.m)
                PermissionRow("Edit any task", "Not just the ones they hold", p.editAnyTask) { onPermissionToggle(p.copy(editAnyTask = it)) }
                InsetDivider(start = Spacing.m)
                PermissionRow("Manage tags", "Create, rename and delete tags", p.manageTags) { onPermissionToggle(p.copy(manageTags = it)) }

                HorizontalDivider(color = Line, modifier = Modifier.padding(top = Spacing.xs))
                TextButton(onClick = onDeleteClick, modifier = Modifier.padding(horizontal = Spacing.xs)) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = CoralInk, modifier = Modifier.size(18.dp))
                    Text("Delete role", color = CoralInk, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(name: String, subtitle: String, checked: Boolean, isDangerous: Boolean = false, onCheckedChange: (Boolean) -> Unit) {
    val haptics = rememberAppHaptics()
    val toggle: (Boolean) -> Unit = {
        if (it) haptics.toggleOn() else haptics.toggleOff()
        onCheckedChange(it)
    }
    // Whole row toggles, not just the small switch.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = androidx.compose.ui.semantics.Role.Switch, onValueChange = toggle)
            .padding(horizontal = Spacing.m, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = Spacing.s)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, color = if (isDangerous) CoralInk else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Ink500)
        }
        TmkSwitch(checked = checked, onCheckedChange = null)
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
