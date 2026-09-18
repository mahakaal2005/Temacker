package com.example.temacker.feature_project.presentation.load

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onFailure
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_project.domain.use_case.ObserveLoadUseCase
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
class LoadViewModel(
    private val observeUserProjects: ObserveUserProjectsUseCase,
    private val observeLoad: ObserveLoadUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoadState())
    val state = _state.asStateFlow()

    init {
        // Same first-project derivation as RosterViewModel — see its comment on why
        // distinctUntilChanged matters (avoids tearing down listeners on unrelated re-emissions).
        val projectId = observeUserProjects()
            .mapNotNull { (it as? Result.Success)?.data?.firstOrNull()?.id }
            .distinctUntilChanged()

        viewModelScope.launch {
            projectId.flatMapLatest { observeLoad(it) }.collect { result ->
                result
                    .onSuccess { loads -> _state.update { it.copy(holderLoads = loads, isLoading = false) } }
                    .onFailure { error -> _state.update { it.copy(error = error.toUiText(), isLoading = false) } }
            }
        }
    }

    fun onAction(action: LoadAction) {
        when (action) {
            LoadAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }
}
