package com.example.temacker.feature_project.presentation.roster

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.components.TmkSheet
import com.example.temacker.core.presentation.components.TmkSwitch
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.RolePermissions
import com.example.temacker.feature_project.presentation.role_copy.PermissionInfo

fun extraPermissionCount(extras: RolePermissions, role: RolePermissions) =
    PermissionInfo.entries.count { it.grantable && it.get(extras) && !it.get(role) }

// Toggle rows shared by this sheet and the member page; rows the role already gives are locked on.
@Composable
fun ExtraPermissionRows(member: Membership, enabled: Boolean, onChange: (RolePermissions) -> Unit) {
    PermissionInfo.entries.filter { it.grantable }.forEach { info ->
        val fromRole = info.get(member.permissions)
        val on = fromRole || info.get(member.extraPermissions)
        val editable = enabled && !fromRole
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = editable) { onChange(info.set(member.extraPermissions, !on)) }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(info.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(if (fromRole) "From ${member.roleName}" else info.subtitle, style = MaterialTheme.typography.bodySmall, color = Ink500)
            }
            TmkSwitch(checked = on, onCheckedChange = { onChange(info.set(member.extraPermissions, it)) }, enabled = editable)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraPermissionsSheet(member: Membership, onChange: (RolePermissions) -> Unit, onDismiss: () -> Unit) {
    TmkSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.l)) {
            Text("Extra permissions", style = MaterialTheme.typography.titleLarge)
            Text(
                "${member.displayName} keeps everything ${member.roleName} gives. These add to it.",
                style = MaterialTheme.typography.bodySmall,
                color = Ink500,
                modifier = Modifier.padding(top = 4.dp, bottom = Spacing.s)
            )
            ExtraPermissionRows(member = member, enabled = true, onChange = onChange)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExtraPermissionsSheetPreview() {
    TemackerTheme {
        Column {
            val m = Membership("p", "u", "r", "Editor", RolePermissions(assignTasks = true), "Daniel", null, 0L, false,
                extraPermissions = RolePermissions(manageInviteCode = true))
            Text("Preview shows the rows only")
            ExtraPermissionsSheet(member = m, onChange = {}, onDismiss = {})
        }
    }
}
