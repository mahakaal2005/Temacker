package com.example.temacker.core.presentation.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

// Soft, navy-tinted shadows per the spec's --e1/--e3 (never plain black).
val LineSoft = NeutralWash

fun Modifier.cardElevation(shape: RoundedCornerShape = RoundedCornerShape(20.dp)) = shadow(
    elevation = 3.dp,
    shape = shape,
    ambientColor = Navy900.copy(alpha = 0.06f),
    spotColor = Navy900.copy(alpha = 0.10f)
)

// For the floating bottom bar, bottom sheets and toasts — a stronger lift than cards.
fun Modifier.floatingElevation(shape: RoundedCornerShape) = shadow(
    elevation = 10.dp,
    shape = shape,
    ambientColor = Navy900.copy(alpha = 0.10f),
    spotColor = Navy900.copy(alpha = 0.20f)
)
