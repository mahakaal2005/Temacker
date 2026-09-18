package com.example.temacker.feature_tasks.presentation.decline

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText

@Stable
data class DeclineState(
    val reason: String = "",
    val isSending: Boolean = false,
    val error: UiText? = null
)

val DECLINE_REASON_CHIPS = listOf("Not my area", "No time this week", "Need more info")
