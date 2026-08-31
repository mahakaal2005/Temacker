package com.example.temacker.feature_project.presentation.manage_roles

sealed interface ManageRolesEvent {
    data object NavigateBack : ManageRolesEvent
}
