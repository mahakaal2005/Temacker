package com.example.temacker.feature_project.presentation.role_explainer

sealed interface RoleExplainerAction {
    data object OnBackClick : RoleExplainerAction
}
