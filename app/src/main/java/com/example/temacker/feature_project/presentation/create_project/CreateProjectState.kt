package com.example.temacker.feature_project.presentation.create_project

import com.example.temacker.core.presentation.util.UiText

data class CreateProjectState(
    val name: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null
)
