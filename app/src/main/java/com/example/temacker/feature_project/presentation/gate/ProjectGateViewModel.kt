package com.example.temacker.feature_project.presentation.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Decides Home vs No-project once, at app entry — screens below don't need to know which.
class ProjectGateViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase
) : ViewModel() {

    private val _events = Channel<ProjectGateEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val projects = observeUserProjects().first()
            _events.send(if (projects.isEmpty()) ProjectGateEvent.NavigateToNoProject else ProjectGateEvent.NavigateToHome)
        }
    }
}
