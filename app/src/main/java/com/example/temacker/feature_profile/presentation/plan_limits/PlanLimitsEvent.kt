package com.example.temacker.feature_profile.presentation.plan_limits

sealed interface PlanLimitsEvent {
    data object NavigateBack : PlanLimitsEvent
}
