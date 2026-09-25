package com.example.temacker.feature_profile.presentation.plan_limits

sealed interface PlanLimitsAction {
    data object OnBackClick : PlanLimitsAction
    data object OnErrorDismissed : PlanLimitsAction
}
