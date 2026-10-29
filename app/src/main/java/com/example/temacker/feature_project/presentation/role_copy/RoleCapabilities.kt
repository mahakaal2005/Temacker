package com.example.temacker.feature_project.presentation.role_copy

import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.RolePermissions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class CapabilityGroup { TEAM, TASKS }

// Every permission flag, worded once for Manage roles, the member page, extras and first run.
enum class PermissionInfo(
    val label: String,
    val subtitle: String,
    val group: CapabilityGroup,
    val get: (RolePermissions) -> Boolean,
    val set: (RolePermissions, Boolean) -> RolePermissions,
    // deleteProject can only come from a role, never as a per-member extra.
    val grantable: Boolean = true,
    val isDangerous: Boolean = false
) {
    INVITE_MEMBERS("Invite members", "Create and share invite codes", CapabilityGroup.TEAM, { it.manageInviteCode }, { p, on -> p.copy(manageInviteCode = on) }),
    CHANGE_ROLES("Change roles", "Change roles and extra permissions", CapabilityGroup.TEAM, { it.manageRoles }, { p, on -> p.copy(manageRoles = on) }),
    REMOVE_MEMBERS("Remove members", "Take someone out of the project", CapabilityGroup.TEAM, { it.removeMembers }, { p, on -> p.copy(removeMembers = on) }),
    DELETE_PROJECT("Delete project", "Permanent, can't be undone", CapabilityGroup.TEAM, { it.deleteProject }, { p, on -> p.copy(deleteProject = on) }, grantable = false, isDangerous = true),
    ASSIGN_TASKS("Create and assign tasks", "Make tasks and give them to anyone", CapabilityGroup.TASKS, { it.assignTasks }, { p, on -> p.copy(assignTasks = on) }),
    EDIT_ANY_TASK("Delete any task", "Not just the ones they hold", CapabilityGroup.TASKS, { it.editAnyTask }, { p, on -> p.copy(editAnyTask = on) }),
    MANAGE_TAGS("Manage tags", "Create, rename and delete tags", CapabilityGroup.TASKS, { it.manageTags }, { p, on -> p.copy(manageTags = on) }),
    IMPORT_TASKS("Import tasks", "Create many tasks from a JSON file", CapabilityGroup.TASKS, { it.importTasks }, { p, on -> p.copy(importTasks = on) }),
    APPROVE_REQUESTS("Approve task requests", "Answer requests to drop or transfer a task", CapabilityGroup.TASKS, { it.approveTaskRequests }, { p, on -> p.copy(approveTaskRequests = on) })
}

// Handing on a task you hold never needs a role, so these are true for everyone.
val ALWAYS_ALLOWED = listOf(
    "See every task and who holds it",
    "Hold a task if someone hands you one",
    "Hand it back, or ask to drop or transfer it"
)

fun RolePermissions.grantedCapabilities(): List<PermissionInfo> = PermissionInfo.entries.filter { it.get(this) }

fun RolePermissions.missingCapabilities(): List<PermissionInfo> = PermissionInfo.entries.filterNot { it.get(this) }

// Extras that add something the role doesn't already give.
fun Membership.addedExtras(): List<PermissionInfo> =
    PermissionInfo.entries.filter { it.grantable && it.get(extraPermissions) && !it.get(permissions) }

fun formatRoleDate(millis: Long, locale: Locale = Locale.getDefault(), zone: TimeZone = TimeZone.getDefault()): String =
    SimpleDateFormat("d MMM", locale).apply { timeZone = zone }.format(Date(millis))

// Null on members whose docs predate Phase 5, so the screens simply omit the line.
fun roleSetLine(membership: Membership, formatDate: (Long) -> String = ::formatRoleDate): String? {
    val by = membership.roleSetByDisplayName ?: return null
    val at = membership.roleSetAt?.let { " · ${formatDate(it)}" }.orEmpty()
    return "Set by $by$at"
}

// Only when someone other than the member set the role, which is the joiner's case.
fun addedByLine(membership: Membership): String? {
    val by = membership.roleSetByDisplayName ?: return null
    return if (membership.roleSetByUid == membership.userId) null else "$by added you a moment ago."
}
