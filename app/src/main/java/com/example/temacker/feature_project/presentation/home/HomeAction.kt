package com.example.temacker.feature_project.presentation.home

sealed interface HomeAction {
    data object OnGoToRosterClick : HomeAction
}
