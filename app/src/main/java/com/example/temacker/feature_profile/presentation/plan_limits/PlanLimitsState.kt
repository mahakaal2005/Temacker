package com.example.temacker.feature_profile.presentation.plan_limits

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText

@Stable
data class PlanLimitsState(
    val projectName: String = "",
    val taskCount: Int = 0,
    val memberCount: Int = 0,
    val isLoading: Boolean = true,
    val error: UiText? = null
)

// Free-tier caps — a budget rather than a market position, per the design spec's Phase 6 thesis.
// No source of truth beyond the mock exists yet; revisit if these need to come from remote config.
const val TASK_CAP = 200
const val MEMBER_CAP = 25
