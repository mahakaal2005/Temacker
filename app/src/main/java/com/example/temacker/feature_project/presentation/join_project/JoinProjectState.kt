package com.example.temacker.feature_project.presentation.join_project

import com.example.temacker.core.presentation.util.UiText

data class JoinProjectState(
    val code: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null
)
