package com.example.temacker.feature_project.presentation.create_project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import com.example.temacker.feature_project.domain.use_case.CreateProjectUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Injects feature_auth's AuthRepository directly to read the creator's displayName/photoUrl for
// the Leader membership doc — same pragmatic cross-feature read already used by ProfileViewModel.
class CreateProjectViewModel(
    private val createProject: CreateProjectUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateProjectState())
    val state = _state.asStateFlow()

    private val _events = Channel<CreateProjectEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateProjectAction) {
        when (action) {
            is CreateProjectAction.OnNameChange -> _state.update { it.copy(name = action.value, error = null) }
            CreateProjectAction.OnCreateClick -> create()
            CreateProjectAction.OnBackClick -> viewModelScope.launch { _events.send(CreateProjectEvent.NavigateBack) }
        }
    }

    private fun create() = viewModelScope.launch {
        val name = _state.value.name.trim()
        if (name.isBlank()) {
            _state.update { it.copy(error = UiText.DynamicString("Enter a project name.")) }
            return@launch
        }

        _state.update { it.copy(isLoading = true, error = null) }
        val user = authRepository.observeUser().first()
        if (user == null) {
            _state.update { it.copy(isLoading = false, error = UiText.DynamicString("You're signed out.")) }
            return@launch
        }

        createProject(name, user.displayName, user.photoUrl)
            .onSuccess { _events.send(CreateProjectEvent.NavigateToHome) }
            .onFailure { error -> _state.update { it.copy(error = error.toUiText()) } }
        _state.update { it.copy(isLoading = false) }
    }
}
