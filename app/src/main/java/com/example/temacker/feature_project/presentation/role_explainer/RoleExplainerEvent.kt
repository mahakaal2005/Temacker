package com.example.temacker.feature_project.presentation.role_explainer

sealed interface RoleExplainerEvent {
    data object NavigateBack : RoleExplainerEvent
}
