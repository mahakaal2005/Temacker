package com.example.temacker.feature_project.presentation.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.ObservePulseUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PulseViewModel(
    private val observePulse: ObservePulseUseCase,
    private val currentProjectProvider: CurrentProjectProvider
) : ViewModel() {

    private val _state = MutableStateFlow(PulseState())
    val state = _state.asStateFlow()

    init {
        val projectId = currentProjectProvider.observeCurrentProjectId()
            .mapNotNull { (it as? Result.Success)?.data }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.flatMapLatest { observePulse(it) }.collect { result ->
                result
                    .onSuccess { events -> _state.update { it.copy(events = events, isLoading = false) } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText(), isLoading = false) } }
            }
        }
    }

    fun onAction(action: PulseAction) {
        when (action) {
            PulseAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }
}
