package com.example.temacker.feature_project.presentation.role_copy

import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.RolePermissions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class CapabilityGroup { TEAM, TASKS }

// The permission flags a person can feel in the app, worded for the first-run and role-explainer screens.
enum class Capability(val label: String, val group: CapabilityGroup) {
    INVITE_MEMBERS("Invite members", CapabilityGroup.TEAM),
    CHANGE_ROLES("Change roles", CapabilityGroup.TEAM),
    REMOVE_MEMBERS("Remove members", CapabilityGroup.TEAM),
    CREATE_TASKS("Create tasks", CapabilityGroup.TASKS),
    DELETE_TASKS("Delete tasks — permanent, cannot be undone", CapabilityGroup.TASKS)
}

// Handing on a task you hold never needs a role, so these are true for everyone.
val ALWAYS_ALLOWED = listOf(
    "See every task and who holds it",
    "Hold a task if someone hands you one",
    "Hand it back, or decline it with a reason"
)

private fun RolePermissions.allows(capability: Capability): Boolean = when (capability) {
    Capability.INVITE_MEMBERS -> manageInviteCode
    Capability.CHANGE_ROLES -> manageRoles
    Capability.REMOVE_MEMBERS -> removeMembers
    Capability.CREATE_TASKS -> assignTasks
    Capability.DELETE_TASKS -> editAnyTask
}

fun RolePermissions.grantedCapabilities(): List<Capability> = Capability.entries.filter { allows(it) }

fun RolePermissions.missingCapabilities(): List<Capability> = Capability.entries.filterNot { allows(it) }

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
