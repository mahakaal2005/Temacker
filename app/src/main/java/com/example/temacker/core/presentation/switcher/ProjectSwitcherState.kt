package com.example.temacker.core.presentation.switcher

import com.example.temacker.core.domain.model.ProjectRef

// The pill's neighbours in Switch project order; null at either end of the list.
data class ProjectSwitcherState(
    val previous: ProjectRef? = null,
    val next: ProjectRef? = null
)
