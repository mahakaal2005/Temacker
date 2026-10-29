package com.example.temacker.feature_tasks.presentation.import_tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_tasks.domain.import_tasks.TaskImportParser
import com.example.temacker.feature_tasks.domain.use_case.ImportTasksUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectMemberUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveProjectMembersUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImportTasksViewModel(
    private val observeCurrentProjectId: ObserveCurrentProjectIdUseCase,
    private val observeCurrentProjectMember: ObserveCurrentProjectMemberUseCase,
    private val observeProjectMembers: ObserveProjectMembersUseCase,
    private val importTasks: ImportTasksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ImportTasksState())
    val state = _state.asStateFlow()

    private val _events = Channel<ImportTasksEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ImportTasksAction) {
        when (action) {
            is ImportTasksAction.OnFileRead -> parse(action.text)
            ImportTasksAction.OnFileUnreadable -> _state.update { it.copy(error = UiText.DynamicString("Couldn't read that file.")) }
            ImportTasksAction.OnImportClick -> import()
            ImportTasksAction.OnChooseAnotherClick -> _state.update { it.copy(preview = null, error = null) }
            ImportTasksAction.OnBackClick -> viewModelScope.launch { _events.send(ImportTasksEvent.NavigateBack) }
            ImportTasksAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun parse(text: String) = viewModelScope.launch {
        val projectId = (observeCurrentProjectId().first() as? Result.Success)?.data
        val me = projectId?.let { (observeCurrentProjectMember(it).first() as? Result.Success)?.data }
        if (projectId == null || me == null) {
            _state.update { it.copy(error = UiText.DynamicString("Couldn't find your project membership.")) }
            return@launch
        }
        val members = (observeProjectMembers(projectId).first() as? Result.Success)?.data.orEmpty()
        _state.update { it.copy(preview = TaskImportParser.parse(text, members, me), error = null) }
    }

    private fun import() = viewModelScope.launch {
        val tasks = _state.value.preview?.tasks?.takeIf { it.isNotEmpty() } ?: return@launch
        _state.update { it.copy(isImporting = true, error = null) }
        val projectId = (observeCurrentProjectId().first() as? Result.Success)?.data
        val me = projectId?.let { (observeCurrentProjectMember(it).first() as? Result.Success)?.data }
        if (projectId == null || me == null) {
            _state.update { it.copy(isImporting = false, error = UiText.DynamicString("Couldn't find your project membership.")) }
            return@launch
        }
        val (_, error) = importTasks(projectId, tasks, me.displayName)
        if (error == null) {
            _events.send(ImportTasksEvent.NavigateToBoard)
        } else {
            _state.update { it.copy(isImporting = false, error = error.toUiText()) }
        }
    }
}
