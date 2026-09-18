package com.example.temacker.feature_project.presentation.stuck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.ObserveStuckHandoffsUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class StuckViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeStuckHandoffs: ObserveStuckHandoffsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StuckState())
    val state = _state.asStateFlow()

    init {
        val projectId = observeUserProjects()
            .mapNotNull { (it as? Result.Success)?.data?.firstOrNull()?.id }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.flatMapLatest { observeStuckHandoffs(it) }.collect { result ->
                result
                    .onSuccess { stuck -> _state.update { it.copy(stuckHandoffs = stuck, isLoading = false) } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText(), isLoading = false) } }
            }
        }
    }

    fun onAction(action: StuckAction) {
        when (action) {
            StuckAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }
}
