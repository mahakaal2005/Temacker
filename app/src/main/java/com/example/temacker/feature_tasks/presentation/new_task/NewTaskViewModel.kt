package com.example.temacker.feature_tasks.presentation.new_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.use_case.CreateTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectMemberUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewTaskViewModel(
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val observeCurrentProjectMember: ObserveCurrentProjectMemberUseCase,
    private val createTask: CreateTaskUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewTaskState())
    val state = _state.asStateFlow()

    private val _events = Channel<NewTaskEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: NewTaskAction) {
        when (action) {
            is NewTaskAction.OnTitleChange -> _state.update { it.copy(title = action.value, error = null) }
            is NewTaskAction.OnDescriptionChange -> _state.update { it.copy(description = action.value) }
            is NewTaskAction.OnDueDateSelected -> _state.update { it.copy(dueDate = action.value) }
            NewTaskAction.OnCreateClick -> create()
            NewTaskAction.OnBackClick -> viewModelScope.launch { _events.send(NewTaskEvent.NavigateBack) }
            NewTaskAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun create() = viewModelScope.launch {
        val title = _state.value.title.trim()
        if (title.isBlank()) {
            _state.update { it.copy(error = UiText.DynamicString("Enter a task title.")) }
            return@launch
        }

        _state.update { it.copy(isLoading = true, error = null) }

        val projectId = (observeCurrentProjectId().first() as? Result.Success)?.data
        val member = (observeCurrentProjectMember(projectId.orEmpty()).first() as? Result.Success)?.data
        if (projectId == null || member == null) {
            _state.update { it.copy(isLoading = false, error = UiText.DynamicString("Couldn't find your project membership.")) }
            return@launch
        }

        createTask(
            projectId = projectId,
            title = title,
            description = _state.value.description.trim().ifBlank { null },
            dueDate = _state.value.dueDate,
            holderUid = member.uid,
            holderDisplayName = member.displayName,
            createdByDisplayName = member.displayName
        ).let { result ->
            when (result) {
                is Result.Success -> _events.send(NewTaskEvent.NavigateToBoard)
                is Result.Error -> _state.update { it.copy(error = result.error.toUiText()) }
            }
        }
        _state.update { it.copy(isLoading = false) }
    }
}
