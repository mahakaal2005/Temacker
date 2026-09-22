package com.example.temacker.feature_project.presentation.switch_project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.repository.SelectedProjectStore
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.model.Project
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SwitchProjectViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeMembers: ObserveMembersUseCase,
    private val observeCurrentMembership: ObserveCurrentMembershipUseCase,
    private val currentProjectProvider: CurrentProjectProvider,
    private val selectedProjectStore: SelectedProjectStore
) : ViewModel() {

    private val _state = MutableStateFlow(SwitchProjectState())
    val state = _state.asStateFlow()

    private val _events = Channel<SwitchProjectEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeUserProjects()
            .combine(currentProjectProvider.observeCurrentProjectId()) { projectsResult, currentIdResult ->
                projectsResult to currentIdResult
            }
            .flatMapLatest { (projectsResult, currentIdResult) -> rowsFlow(projectsResult, currentIdResult) }
            .onEach { result ->
                result
                    .onSuccess { rows -> _state.update { it.copy(rows = rows, isLoading = false, error = null) } }
                    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toUiText()) } }
            }
            .launchIn(viewModelScope)
    }

    private fun rowsFlow(
        projectsResult: Result<List<Project>, DataError>,
        currentIdResult: Result<String?, DataError>
    ): Flow<Result<List<SwitchProjectRow>, DataError>> {
        if (projectsResult is Result.Error) return flowOf(Result.Error(projectsResult.error))
        val projects = (projectsResult as Result.Success).data
        if (projects.isEmpty()) return flowOf(Result.Success(emptyList()))
        val currentId = (currentIdResult as? Result.Success)?.data
        val rowFlows = projects.map { project -> rowFlow(project, currentId) }
        return combine(rowFlows) { rows -> Result.Success(rows.toList()) }
    }

    private fun rowFlow(project: Project, currentId: String?): Flow<SwitchProjectRow> {
        return observeMembers(project.id).combine(observeCurrentMembership(project.id)) { membersResult, membershipResult ->
            val memberCount = (membersResult as? Result.Success)?.data?.size ?: 0
            val roleName = (membershipResult as? Result.Success)?.data?.roleName.orEmpty()
            SwitchProjectRow(
                projectId = project.id,
                name = project.name,
                memberCount = memberCount,
                roleName = roleName,
                isSelected = project.id == currentId
            )
        }
    }

    fun onAction(action: SwitchProjectAction) {
        when (action) {
            is SwitchProjectAction.OnProjectClick -> viewModelScope.launch {
                selectedProjectStore.setSelectedProjectId(action.projectId)
                _events.send(SwitchProjectEvent.NavigateBack)
            }
        }
    }
}
