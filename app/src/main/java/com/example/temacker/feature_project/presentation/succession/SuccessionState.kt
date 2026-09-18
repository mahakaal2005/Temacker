package com.example.temacker.feature_project.presentation.succession

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText

enum class SuccessionStep { NAME, CONFIRM }

@Stable
data class SuccessionState(
    val newProjectName: String = "",
    val step: SuccessionStep = SuccessionStep.NAME,
    val isSubmitting: Boolean = false,
    val error: UiText? = null
)
