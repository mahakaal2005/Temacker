package com.example.temacker.feature_profile.presentation.plan_limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_profile.domain.use_case.GetProjectExportUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlanLimitsViewModel(
    private val getProjectExport: GetProjectExportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PlanLimitsState())
    val state = _state.asStateFlow()

    private val _events = Channel<PlanLimitsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            getProjectExport().collect { result ->
                result.onSuccess { data ->
                    _state.update {
                        it.copy(projectName = data.projectName, taskCount = data.taskCount, memberCount = data.memberCount, isLoading = false)
                    }
                }
                if (result is Result.Error) _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
            }
        }
    }

    fun onAction(action: PlanLimitsAction) {
        when (action) {
            PlanLimitsAction.OnBackClick -> viewModelScope.launch { _events.send(PlanLimitsEvent.NavigateBack) }
            PlanLimitsAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }
}
