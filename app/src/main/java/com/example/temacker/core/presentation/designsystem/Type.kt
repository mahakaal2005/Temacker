package com.example.temacker.core.presentation.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// Sans maps to Android's system Roboto (spec's --sans). Mono approximates the spec's Roboto
// Mono with the platform monospace family — no bundled font file for it yet.
val MonoFontFamily = FontFamily.Monospace

// Mirrors the spec's h1/h2/h3/p/small/mono-lbl roles (specs/UI/temacker-all-phases-android-concept.html).
val Typography = Typography(
    headlineMedium = TextStyle( // .h1
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).toFloat().em
    ),
    titleLarge = TextStyle( // .h2
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.01).toFloat().em
    ),
    titleMedium = TextStyle( // .h3
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 21.sp
    ),
    bodyLarge = TextStyle( // .p
        fontFamily = FontFamily.Default,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle( // .small
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelSmall = TextStyle( // .mono-lbl — call sites apply uppercase
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp
    )
)
