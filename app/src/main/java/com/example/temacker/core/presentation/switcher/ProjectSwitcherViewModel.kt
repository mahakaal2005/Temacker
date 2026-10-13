package com.example.temacker.core.presentation.switcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.model.ProjectRef
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.repository.SelectedProjectStore
import com.example.temacker.core.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// Shared by the four pill screens, which live in three features (architecture §4 allowance).
class ProjectSwitcherViewModel(
    currentProjectProvider: CurrentProjectProvider,
    private val selectedProjectStore: SelectedProjectStore
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectSwitcherState())
    val state = _state.asStateFlow()

    init {
        currentProjectProvider.observeUserProjectRefs()
            .combine(currentProjectProvider.observeCurrentProjectId()) { refsResult, idResult ->
                // Errors keep the last neighbours rather than blanking the gesture.
                val refs = (refsResult as? Result.Success)?.data ?: return@combine null
                val currentId = (idResult as? Result.Success)?.data
                neighboursOf(refs, currentId)
            }
            .onEach { neighbours -> if (neighbours != null) _state.value = neighbours }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ProjectSwitcherAction) {
        val target = when (action) {
            ProjectSwitcherAction.OnSwipeToPrevious -> _state.value.previous
            ProjectSwitcherAction.OnSwipeToNext -> _state.value.next
        } ?: return
        viewModelScope.launch { selectedProjectStore.setSelectedProjectId(target.id) }
    }

    private fun neighboursOf(refs: List<ProjectRef>, currentId: String?): ProjectSwitcherState {
        val index = refs.indexOfFirst { it.id == currentId }
        if (index == -1) return ProjectSwitcherState()
        return ProjectSwitcherState(previous = refs.getOrNull(index - 1), next = refs.getOrNull(index + 1))
    }
}
