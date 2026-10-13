package com.example.temacker.feature_project.presentation.gate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.domain.use_case.SyncUserProjectsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// Decides Home vs No-project once, at app entry — screens below don't need to know which.
class ProjectGateViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val syncUserProjects: SyncUserProjectsUseCase
) : ViewModel() {

    private val _events = Channel<ProjectGateEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            // Cached projects mean a returning user, so go straight in.
            if (cachedProjects().isNotEmpty()) {
                _events.send(ProjectGateEvent.NavigateToHome)
                return@launch
            }
            // An empty cache isn't proof of no projects (fresh install), so let the server answer first.
            syncUserProjects()
            _events.send(if (cachedProjects().isEmpty()) ProjectGateEvent.NavigateToNoProject else ProjectGateEvent.NavigateToHome)
        }
    }

    // Skips transient Result.Error blips and reads the first Room-backed Success.
    private suspend fun cachedProjects(): List<Project> = observeUserProjects()
        .filterIsInstance<Result.Success<List<Project>>>()
        .map { it.data }
        .first()
}
