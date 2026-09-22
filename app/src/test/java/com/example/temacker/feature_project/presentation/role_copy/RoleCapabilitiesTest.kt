package com.example.temacker.feature_project.presentation.role_copy

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.example.temacker.feature_project.domain.model.Membership
import com.example.temacker.feature_project.domain.model.RolePermissions
import org.junit.jupiter.api.Test

class RoleCapabilitiesTest {

    private fun member(
        setByUid: String? = null,
        setByName: String? = null,
        setAt: Long? = null,
        userId: String = "u2"
    ) = Membership(
        "p1", userId, "r1", "Default", RolePermissions.NONE, "Mei", null, 0L, false,
        roleSetByUid = setByUid, roleSetByDisplayName = setByName, roleSetAt = setAt
    )

    @Test
    fun `no permissions grants nothing and misses everything`() {
        assertThat(RolePermissions.NONE.grantedCapabilities()).isEmpty()
        assertThat(RolePermissions.NONE.missingCapabilities()).containsExactlyInAnyOrder(*Capability.entries.toTypedArray())
    }

    @Test
    fun `leader grants everything and misses nothing`() {
        assertThat(RolePermissions.ALL_GRANTED.grantedCapabilities()).containsExactlyInAnyOrder(*Capability.entries.toTypedArray())
        assertThat(RolePermissions.ALL_GRANTED.missingCapabilities()).isEmpty()
    }

    @Test
    fun `each flag maps to its own capability`() {
        val role = RolePermissions(manageRoles = true, assignTasks = true)
        assertThat(role.grantedCapabilities()).containsExactly(Capability.CHANGE_ROLES, Capability.CREATE_TASKS)
        assertThat(role.missingCapabilities()).containsExactly(
            Capability.INVITE_MEMBERS, Capability.REMOVE_MEMBERS, Capability.DELETE_TASKS
        )
    }

    @Test
    fun `set-by line names the setter and date when both are known`() {
        assertThat(roleSetLine(member(setByName = "Priya", setAt = 1L)) { "14 Aug" }).isEqualTo("Set by Priya · 14 Aug")
    }

    @Test
    fun `set-by line drops the date when it is unknown`() {
        assertThat(roleSetLine(member(setByName = "Priya")) { "14 Aug" }).isEqualTo("Set by Priya")
    }

    @Test
    fun `set-by line is null on members from before the stamp existed`() {
        assertThat(roleSetLine(member())).isNull()
    }

    @Test
    fun `added-by line shows for an inviter and hides for self or unknown`() {
        assertThat(addedByLine(member(setByUid = "u1", setByName = "Priya"))).isEqualTo("Priya added you a moment ago.")
        assertThat(addedByLine(member(setByUid = "u2", setByName = "Mei"))).isNull()
        assertThat(addedByLine(member())).isNull()
    }
}
