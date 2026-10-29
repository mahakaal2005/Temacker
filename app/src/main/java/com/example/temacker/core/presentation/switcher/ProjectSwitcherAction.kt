package com.example.temacker.core.presentation.switcher

sealed interface ProjectSwitcherAction {
    data object OnSwipeToPrevious : ProjectSwitcherAction
    data object OnSwipeToNext : ProjectSwitcherAction
}
