package com.example.temacker.core.presentation.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

// The only place haptic types get chosen — see phase-8-ui-redesign.md §5 for the full map.
// Less is more: only the baton hero moments and tab/toggle ticks vibrate, nothing else.
@Stable
class AppHaptics(private val haptic: HapticFeedback) {
    // Accept, send handoff, mark done, create task/project, join project, decline, copy code.
    fun confirm() = haptic.performHapticFeedback(HapticFeedbackType.Confirm)

    // Validation error, failed action, permission blocked.
    fun reject() = haptic.performHapticFeedback(HapticFeedbackType.Reject)

    // Bottom-bar tab change, segmented-tab change.
    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)

    fun toggleOn() = haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
    fun toggleOff() = haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)
}

@Composable
fun rememberAppHaptics(): AppHaptics {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { AppHaptics(haptic) }
}
