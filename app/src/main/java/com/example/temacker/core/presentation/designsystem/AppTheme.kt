package com.example.temacker.core.presentation.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Primarily light — warm paper surfaces, per the spec (specs/UI/temacker-all-phases-android-concept.html).
// Dark treatment is reserved for specific screens the spec itself renders dark (splash, Phase 3
// lock screen), applied locally on those screens rather than as a whole second app theme.
private val AppColorScheme = lightColorScheme(
    primary = Amber,
    onPrimary = Navy900,
    primaryContainer = AmberWash,
    onPrimaryContainer = AmberInk,
    secondary = Teal,
    onSecondary = White,
    secondaryContainer = TealWash,
    onSecondaryContainer = TealInk,
    background = Paper,
    onBackground = Ink900,
    surface = White,
    onSurface = Ink900,
    surfaceVariant = NeutralWash,
    onSurfaceVariant = Ink600,
    outline = Line,
    error = Coral,
    onError = White,
    errorContainer = CoralWash,
    onErrorContainer = CoralInk
)

@Composable
fun TemackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
