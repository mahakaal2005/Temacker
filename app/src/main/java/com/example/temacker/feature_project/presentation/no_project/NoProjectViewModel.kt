package com.example.temacker.feature_project.presentation.no_project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Pure navigation fork — no state of its own (mirrors the mock's "First step" screen).
class NoProjectViewModel : ViewModel() {

    private val _events = Channel<NoProjectEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: NoProjectAction) = viewModelScope.launch {
        when (action) {
            NoProjectAction.OnCreateProjectClick -> _events.send(NoProjectEvent.NavigateToCreateProject)
            NoProjectAction.OnJoinProjectClick -> _events.send(NoProjectEvent.NavigateToJoinProject)
            NoProjectAction.OnProfileClick -> _events.send(NoProjectEvent.NavigateToProfile)
        }
    }
}
