package com.example.temacker.feature_project.presentation.member_detail

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.Avatar
import com.example.temacker.core.presentation.components.AvatarTone
import com.example.temacker.core.presentation.components.ChipTone
import com.example.temacker.core.presentation.components.ConfirmDialog
import com.example.temacker.core.presentation.components.LoadingState
import com.example.temacker.core.presentation.components.SectionLabel
import com.example.temacker.core.presentation.components.StatusChip
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.components.TmkButtonVariant
import com.example.temacker.core.presentation.components.TmkSheet
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.White
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.Role
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.presentation.role_copy.ALWAYS_ALLOWED
import com.example.temacker.feature_project.presentation.role_copy.CapabilityGroup
import com.example.temacker.feature_project.presentation.role_copy.CapabilityRow
import com.example.temacker.feature_project.presentation.role_copy.PermissionInfo
import com.example.temacker.feature_project.presentation.role_copy.addedExtras
import com.example.temacker.feature_project.presentation.role_copy.formatRoleDate
import com.example.temacker.feature_project.presentation.role_copy.roleSetLine
import com.example.temacker.feature_project.presentation.roster.ExtraPermissionRows
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val CardShape = RoundedCornerShape(18.dp)

@Composable
fun MemberDetailRoot(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: MemberDetailViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            MemberDetailEvent.NavigateBack -> onNavigateBack()
        }
    }

    MemberDetailScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(state: MemberDetailState, onAction: (MemberDetailAction) -> Unit) {
    val member = state.member
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(member?.displayName ?: "Member", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { onAction(MemberDetailAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    if (member != null) TextButton(onClick = { onAction(MemberDetailAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }
            when {
                state.isLoading -> LoadingState(icon = Icons.Rounded.Person, title = "Loading member", body = "Getting their role and permissions…")
                member != null -> MemberContent(state, member, onAction)
            }
        }
    }

    if (state.isRolePickerVisible && member != null) {
        RolePickerSheet(
            roles = state.roles.filterNot { it.isLeader },
            currentRoleId = member.roleId,
            onSelect = { onAction(MemberDetailAction.OnRoleSelected(it)) },
            onDismiss = { onAction(MemberDetailAction.OnDismissRolePicker) }
        )
    }
    when (state.confirm) {
        MemberConfirm.REMOVE -> ConfirmDialog(
            title = "Remove ${member?.displayName}?",
            text = "Their unfinished tasks go to the Leader. They can rejoin with a new invite code.",
            confirmLabel = "Remove",
            isDestructive = true,
            onConfirm = { onAction(MemberDetailAction.OnConfirmClick) },
            onDismiss = { onAction(MemberDetailAction.OnDismissConfirm) }
        )
        MemberConfirm.MAKE_LEADER -> ConfirmDialog(
            title = "Make ${member?.displayName} the Leader?",
            text = "They get full access and you move to their role. Only the new Leader can hand it back.",
            confirmLabel = "Make Leader",
            onConfirm = { onAction(MemberDetailAction.OnConfirmClick) },
            onDismiss = { onAction(MemberDetailAction.OnDismissConfirm) }
        )
        MemberConfirm.NONE -> Unit
    }
}

@Composable
private fun MemberContent(state: MemberDetailState, member: Membership, onAction: (MemberDetailAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        ProfileCard(member)

        SectionLabel("Role")
        RoleCard(
            member = member,
            expanded = state.isRoleExpanded,
            canChange = state.canEdit,
            onToggle = { onAction(MemberDetailAction.OnToggleRole) },
            onChangeRole = { onAction(MemberDetailAction.OnChangeRoleClick) }
        )

        if (!member.isLeader) {
            SectionLabel("Extra permissions")
            WhiteCard {
                if (state.canEdit) {
                    Column(modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs)) {
                        Text(
                            "Add a permission just for ${member.displayName}, on top of ${member.roleName}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink500,
                            modifier = Modifier.padding(vertical = Spacing.xs)
                        )
                        ExtraPermissionRows(member = member, enabled = true) { onAction(MemberDetailAction.OnExtrasChanged(it)) }
                    }
                } else {
                    val extras = member.addedExtras()
                    Column(modifier = Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        if (extras.isEmpty()) Text("None. Everything comes from ${member.roleName}.", style = MaterialTheme.typography.bodyMedium, color = Ink500)
                        extras.forEach { CapabilityRow(label = it.label, allowed = true) }
                    }
                }
            }
        }

        SectionLabel("Everyone can")
        WhiteCard {
            Column(modifier = Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                ALWAYS_ALLOWED.forEach { CapabilityRow(label = it, allowed = true) }
            }
        }

        if (!member.isLeader && !state.isSelf && (state.viewerIsLeader || state.canRemove)) {
            Spacer(Modifier.height(Spacing.s))
            if (state.viewerIsLeader) {
                TmkButton(
                    text = "Make Leader",
                    onClick = { onAction(MemberDetailAction.OnMakeLeaderClick) },
                    variant = TmkButtonVariant.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.canRemove) {
                TmkButton(
                    text = "Remove from project",
                    onClick = { onAction(MemberDetailAction.OnRemoveClick) },
                    variant = TmkButtonVariant.DESTRUCTIVE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.navigationBarsPadding().height(Spacing.xl))
    }
}

@Composable
private fun WhiteCard(content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(White, CardShape).border(1.dp, Line, CardShape).clip(CardShape)) { content() }
}

@Composable
private fun ProfileCard(member: Membership) {
    val effective = member.effectivePermissions
    val total = PermissionInfo.entries.size
    val extraCount = member.addedExtras().size
    WhiteCard {
        Row(modifier = Modifier.padding(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
            Avatar(name = member.displayName, size = 52.dp, tone = if (member.isLeader) AvatarTone.ACCENT else AvatarTone.NEUTRAL)
            Column(modifier = Modifier.padding(start = Spacing.s).weight(1f)) {
                Text(member.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (member.joinedAt > 0) Text("Joined ${formatRoleDate(member.joinedAt)}", style = MaterialTheme.typography.bodySmall, color = Ink500)
                StatusChip(
                    text = member.roleName + if (extraCount > 0) " + $extraCount extra" else "",
                    tone = if (member.isLeader) ChipTone.WARNING else ChipTone.NEUTRAL,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        HorizontalDivider(color = Line)
        Text(
            if (member.isLeader) "Can do everything in this project"
            else "Can do ${PermissionInfo.entries.count { it.get(effective) }} of $total things that need permission",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
        )
    }
}

@Composable
private fun RoleCard(member: Membership, expanded: Boolean, canChange: Boolean, onToggle: () -> Unit, onChangeRole: () -> Unit) {
    val role = member.permissions
    val granted = PermissionInfo.entries.count { it.get(role) }
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, label = "memberRoleChevron")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, CardShape)
            .border(1.dp, if (expanded) Amber else Line, CardShape)
            .clip(CardShape)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClickLabel = if (expanded) "Collapse" else "Show what ${member.roleName} gives", onClick = onToggle)
                .padding(horizontal = Spacing.m, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(member.roleName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (member.isLeader) "Full access, always" else "$granted of ${PermissionInfo.entries.size} permissions",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink500
                )
            }
            Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = Ink500, modifier = Modifier.rotate(chevron))
        }
        if (expanded) {
            HorizontalDivider(color = Line)
            Column(modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs)) {
                CapabilityGroup.entries.forEach { group ->
                    SectionLabel(if (group == CapabilityGroup.TEAM) "Team" else "Tasks")
                    PermissionInfo.entries.filter { it.group == group }.forEach { info ->
                        CapabilityRow(
                            label = info.label,
                            allowed = member.isLeader || info.get(role),
                            lockedColor = Ink500,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
                roleSetLine(member)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Ink500, modifier = Modifier.padding(top = Spacing.xs)) }
                if (canChange) {
                    TmkButton(
                        text = "Change role",
                        onClick = onChangeRole,
                        variant = TmkButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.s)
                    )
                } else {
                    Spacer(Modifier.height(Spacing.xs))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RolePickerSheet(roles: List<Role>, currentRoleId: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    TmkSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.l)) {
            Text("Change role", style = MaterialTheme.typography.titleLarge)
            roles.forEach { role ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(role.id) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = role.id == currentRoleId,
                        onClick = { onSelect(role.id) },
                        colors = RadioButtonDefaults.colors(selectedColor = Amber)
                    )
                    Column(modifier = Modifier.padding(start = Spacing.xs)) {
                        Text(role.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            "${PermissionInfo.entries.count { it.get(role.permissions) }} of ${PermissionInfo.entries.size} permissions",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink500
                        )
                    }
                }
            }
        }
    }
}

private val previewMember = Membership(
    "p1", "u2", "r2", "Editor", RolePermissions(assignTasks = true, manageInviteCode = true), "Atul Kumar Singh", null,
    1790000000000L, false, roleSetByDisplayName = "Rudra Sharma", roleSetAt = 1790000000000L,
    extraPermissions = RolePermissions(importTasks = true)
)

@Preview(showBackground = true)
@Composable
private fun MemberDetailEditablePreview() {
    TemackerTheme {
        MemberDetailScreen(
            state = MemberDetailState(member = previewMember, canManageRoles = true, canRemove = true, viewerIsLeader = true, isLoading = false),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberDetailRoleExpandedPreview() {
    TemackerTheme {
        MemberDetailScreen(
            state = MemberDetailState(member = previewMember, canManageRoles = true, isRoleExpanded = true, isLoading = false),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberDetailReadOnlyPreview() {
    TemackerTheme { MemberDetailScreen(state = MemberDetailState(member = previewMember, isLoading = false), onAction = {}) }
}

@Preview(showBackground = true)
@Composable
private fun MemberDetailLeaderPreview() {
    TemackerTheme {
        MemberDetailScreen(
            state = MemberDetailState(
                member = previewMember.copy(roleName = "Leader", isLeader = true, permissions = RolePermissions.ALL_GRANTED),
                canManageRoles = true,
                isRoleExpanded = true,
                isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemberDetailLoadingPreview() {
    TemackerTheme { MemberDetailScreen(state = MemberDetailState(), onAction = {}) }
}

@Preview(showBackground = true)
@Composable
private fun MemberDetailErrorPreview() {
    TemackerTheme {
        MemberDetailScreen(
            state = MemberDetailState(isLoading = false, error = UiText.DynamicString("This member is no longer in the project.")),
            onAction = {}
        )
    }
}
