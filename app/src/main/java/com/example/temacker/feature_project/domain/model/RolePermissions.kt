package com.example.temacker.feature_project.domain.model

data class RolePermissions(
    val manageRoles: Boolean = false,
    val manageInviteCode: Boolean = false,
    val removeMembers: Boolean = false,
    val deleteProject: Boolean = false,
    val assignTasks: Boolean = false,
    val editAnyTask: Boolean = false,
    val manageTags: Boolean = false,
    val importTasks: Boolean = false,
    val approveTaskRequests: Boolean = false
) {
    // Union of two grants; extras are add-only so effective = role + extras.
    operator fun plus(other: RolePermissions) = RolePermissions(
        manageRoles || other.manageRoles,
        manageInviteCode || other.manageInviteCode,
        removeMembers || other.removeMembers,
        deleteProject || other.deleteProject,
        assignTasks || other.assignTasks,
        editAnyTask || other.editAnyTask,
        manageTags || other.manageTags,
        importTasks || other.importTasks,
        approveTaskRequests || other.approveTaskRequests
    )

    // Compact form for the Room column that stores a member's extras.
    fun toBits(): Int = listOf(
        manageRoles, manageInviteCode, removeMembers, deleteProject, assignTasks,
        editAnyTask, manageTags, importTasks, approveTaskRequests
    ).foldIndexed(0) { i, acc, on -> if (on) acc or (1 shl i) else acc }

    companion object {
        val ALL_GRANTED = RolePermissions(true, true, true, true, true, true, true, true, true)
        val NONE = RolePermissions()

        fun fromBits(bits: Int) = RolePermissions(
            manageRoles = bits and 1 != 0,
            manageInviteCode = bits and 2 != 0,
            removeMembers = bits and 4 != 0,
            deleteProject = bits and 8 != 0,
            assignTasks = bits and 16 != 0,
            editAnyTask = bits and 32 != 0,
            manageTags = bits and 64 != 0,
            importTasks = bits and 128 != 0,
            approveTaskRequests = bits and 256 != 0
        )
    }
}
