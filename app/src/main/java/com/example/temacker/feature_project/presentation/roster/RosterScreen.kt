package com.example.temacker.feature_project.presentation.roster

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.components.Avatar
import com.example.temacker.core.presentation.components.AvatarTone
import com.example.temacker.core.presentation.components.ChipTone
import com.example.temacker.core.presentation.components.InsetGroup
import com.example.temacker.core.presentation.components.ListRow
import com.example.temacker.core.presentation.components.StatusChip
import com.example.temacker.core.presentation.components.TmkSheet
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions

// Roster's own tab inside the Team screen (TabRow/HorizontalPager host) — no longer owns a
// top-level AppScaffold/bottom nav, see feature_project/presentation/team/TeamScreen.kt.
@Composable
fun RosterTabContent(
    state: RosterState,
    onAction: (RosterAction) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${state.members.size} members",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                    modifier = Modifier.weight(1f)
                )
                if (state.canManageInvite) {
                    FilledTonalButton(onClick = { onAction(RosterAction.OnInviteClick) }) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.padding(end = Spacing.xxs))
                        Text("Invite")
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
                    if (state.canManageRoles) {
                        item(key = "manage-roles") {
                            InsetGroup(modifier = Modifier.padding(bottom = Spacing.s)) {
                                ListRow(
                                    headline = "Manage roles",
                                    trailing = { Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500) },
                                    onClick = { onAction(RosterAction.OnManageRolesClick) }
                                )
                            }
                        }
                    }
                    items(state.members, key = { it.userId }) { member ->
                        MemberRow(
                            member = member,
                            canAct = state.canRemoveMembers || state.canManageRoles,
                            isMenuOpen = state.menuForUserId == member.userId,
                            canReassign = state.canManageRoles,
                            canRemove = state.canRemoveMembers,
                            canTransfer = state.isLeader,
                            onClick = { onAction(RosterAction.OnMemberClick(member.userId)) },
                            onMoreClick = { onAction(RosterAction.OnMemberMoreClick(member.userId)) },
                            onDismissMenu = { onAction(RosterAction.OnDismissMemberMenu) },
                            onReassignClick = { onAction(RosterAction.OnReassignRoleClick(member.userId)) },
                            onMakeLeaderClick = { onAction(RosterAction.OnMakeLeaderClick(member.userId)) },
                            onRemoveClick = { onAction(RosterAction.OnRemoveMemberClick(member.userId)) }
                        )
                        HorizontalDivider(color = Line)
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

    state.transferTargetUserId?.let { targetId ->
        TransferLeadershipDialog(
            memberName = state.members.firstOrNull { it.userId == targetId }?.displayName ?: "this member",
            onConfirm = { onAction(RosterAction.OnConfirmTransferLeadership) },
            onDismiss = { onAction(RosterAction.OnDismissTransferDialog) }
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
    canTransfer: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onReassignClick: () -> Unit,
    onMakeLeaderClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val isLeader = member.isLeader
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "See what ${member.roleName} can do", onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = member.displayName, tone = if (isLeader) AvatarTone.ACCENT else AvatarTone.NEUTRAL)
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(member.displayName, style = MaterialTheme.typography.bodyLarge)
            StatusChip(
                text = member.roleName,
                tone = if (isLeader) ChipTone.WARNING else ChipTone.NEUTRAL,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (canAct && !isLeader) {
            Box {
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = isMenuOpen, onDismissRequest = onDismissMenu) {
                    if (canReassign) {
                        DropdownMenuItem(text = { Text("Reassign role") }, onClick = onReassignClick)
                    }
                    if (canTransfer) {
                        DropdownMenuItem(text = { Text("Make Leader") }, onClick = onMakeLeaderClick)
                    }
                    if (canRemove) {
                        DropdownMenuItem(
                            text = { Text("Remove from project", color = MaterialTheme.colorScheme.error) },
                            onClick = onRemoveClick
                        )
                    }
                }
            }
        } else {
            // Chevron hint — tapping the row does something (opens the role explainer) even when there's no menu.
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500)
        }
    }
}

@Composable
private fun InviteCodeSheet(
    code: String?,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onGenerateNew: () -> Unit
) {
    TmkSheet(onDismissRequest = onDismiss) {
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
private fun TransferLeadershipDialog(
    memberName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Make $memberName the Leader?") },
        text = {
            Text(
                "They'll get every Leader permission and you'll become a regular member. " +
                    "Only they can hand it back. You can leave the project afterwards if you want."
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Make Leader") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ReassignRoleDialog(
    roles: List<Role>,
    onDismiss: () -> Unit,
    onRoleSelected: (String) -> Unit
) {
    TmkSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.l)) {
            Text("Reassign role", style = MaterialTheme.typography.titleLarge)
            roles.filterNot { it.isLeader }.forEach { role ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRoleSelected(role.id) }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = false, onClick = { onRoleSelected(role.id) })
                    Text(role.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = Spacing.s))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterTabContentPreview() {
    TemackerTheme {
        RosterTabContent(
            state = RosterState(
                isLoading = false,
                canManageInvite = true,
                canRemoveMembers = true,
                canManageRoles = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0, isLeader = true),
                    Membership("p1", "u2", "r2", "Editor", RolePermissions(), "Daniel Osei", null, 0, isLeader = false)
                ),
                roles = listOf(
                    Role("r1", "p1", "Leader", RolePermissions.ALL_GRANTED, isLeader = true),
                    Role("r2", "p1", "Editor", RolePermissions(), isLeader = false)
                )
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterTabContentLoadingPreview() {
    TemackerTheme {
        RosterTabContent(state = RosterState(isLoading = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterTabContentInviteSheetPreview() {
    TemackerTheme {
        RosterTabContent(
            state = RosterState(
                isLoading = false,
                canManageInvite = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0, isLeader = true)
                ),
                isInviteSheetVisible = true,
                inviteCode = "AB12CD34"
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterTabContentReassignDialogPreview() {
    TemackerTheme {
        RosterTabContent(
            state = RosterState(
                isLoading = false,
                canManageRoles = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0, isLeader = true),
                    Membership("p1", "u2", "r2", "Editor", RolePermissions(), "Daniel Osei", null, 0, isLeader = false)
                ),
                roles = listOf(
                    Role("r1", "p1", "Leader", RolePermissions.ALL_GRANTED, isLeader = true),
                    Role("r2", "p1", "Editor", RolePermissions(), isLeader = false)
                ),
                reassignTargetUserId = "u2"
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterTabContentTransferDialogPreview() {
    TemackerTheme {
        RosterTabContent(
            state = RosterState(
                isLoading = false,
                isLeader = true,
                canManageRoles = true,
                canRemoveMembers = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0, isLeader = true),
                    Membership("p1", "u2", "r2", "Editor", RolePermissions(), "Daniel Osei", null, 0, isLeader = false)
                ),
                transferTargetUserId = "u2"
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RosterTabContentErrorPreview() {
    TemackerTheme {
        RosterTabContent(
            state = RosterState(
                isLoading = false,
                canRemoveMembers = true,
                canManageRoles = true,
                members = listOf(
                    Membership("p1", "u1", "r1", "Leader", RolePermissions.ALL_GRANTED, "Priya Raman", null, 0, isLeader = true)
                ),
                error = UiText.DynamicString("Couldn't remove member. Check your connection and try again.")
            ),
            onAction = {}
        )
    }
}
