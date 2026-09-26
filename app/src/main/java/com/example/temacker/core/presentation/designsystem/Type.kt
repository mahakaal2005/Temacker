package com.example.temacker.core.presentation.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.temacker.R

// Sans maps to Android's system Roboto (spec's --sans). Mono is the bundled Roboto Mono (Phase 8).
val MonoFontFamily = FontFamily(Font(R.font.roboto_mono_medium, FontWeight.Medium))

// Mirrors the spec's h1/h2/h3/p/small/mono-lbl roles (specs/UI/temacker-all-phases-android-concept.html).
// Phase 8 fills in the roles screens were already using but that fell back to M3 defaults.
val Typography = Typography(
    displayLarge = TextStyle( // Splash wordmark — was a hardcoded 42.sp
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        letterSpacing = (-0.035).toFloat().em
    ),
    displaySmall = TextStyle( // Login wordmark — was a hardcoded 38.sp
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        letterSpacing = (-0.035).toFloat().em
    ),
    headlineMedium = TextStyle( // .h1
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).toFloat().em
    ),
    headlineSmall = TextStyle( // empty/section headlines ("Nothing to hand over yet")
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).toFloat().em
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
    titleSmall = TextStyle( // row titles (list rows, sheet headers)
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
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
    bodySmall = TextStyle( // meta lines under a row title
        fontFamily = FontFamily.Default,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle( // role pills, section headers
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle( // small mono labels between labelLarge and labelSmall
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.sp
    ),
    labelSmall = TextStyle( // .mono-lbl — call sites apply uppercase
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp
    )
)
