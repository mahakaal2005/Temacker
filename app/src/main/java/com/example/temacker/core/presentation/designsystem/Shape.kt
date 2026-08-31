package com.example.temacker.core.presentation.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Radii from the spec's --r-* tokens (specs/UI/temacker-all-phases-android-concept.html).
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp), // --r-chip
    small = RoundedCornerShape(16.dp), // --r-field
    medium = RoundedCornerShape(18.dp), // --r-btn
    large = RoundedCornerShape(20.dp), // --r-card
    extraLarge = RoundedCornerShape(28.dp) // --r-sheet
)
