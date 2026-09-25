package com.example.temacker.core.presentation.designsystem

import android.provider.Settings
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

// Calm spring for most position/size/shape changes — a little settle, no overshoot.
fun <T> spatialDefault(): SpringSpec<T> = spring(dampingRatio = 0.9f, stiffness = 380f)

// Bouncier spring, reserved for the four baton hero moments (offer/accept/decline/done).
fun <T> spatialExpressive(): SpringSpec<T> = spring(dampingRatio = 0.6f, stiffness = 380f)

// No overshoot allowed — color/alpha only.
fun <T> effectsSpring(): SpringSpec<T> = spring(dampingRatio = 1f, stiffness = 1500f)

val enterTween = tween<Float>(durationMillis = 220)
val exitTween = tween<Float>(durationMillis = 160)

// System "remove animations" setting — springs snap instead of bouncing when this is true.
@Composable
fun rememberReducedMotion(): Boolean {
    if (LocalInspectionMode.current) return false
    val context = LocalContext.current
    val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    return scale == 0f
}
