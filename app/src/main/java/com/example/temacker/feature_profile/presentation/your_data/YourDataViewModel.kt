package com.example.temacker.feature_profile.presentation.your_data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.core.domain.util.Result
import com.example.temacker.core.domain.util.onSuccess
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.core.presentation.util.toUiText
import com.example.temacker.feature_profile.domain.model.DeleteAccountResult
import com.example.temacker.feature_profile.domain.model.ProjectExportSnapshot
import com.example.temacker.feature_profile.domain.use_case.DeleteAccountUseCase
import com.example.temacker.feature_profile.domain.use_case.ExportProjectDataUseCase
import com.example.temacker.feature_profile.domain.use_case.GetProjectExportUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class YourDataViewModel(
    private val getProjectExport: GetProjectExportUseCase,
    private val exportProjectData: ExportProjectDataUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(YourDataState())
    val state = _state.asStateFlow()

    private val _events = Channel<YourDataEvent>()
    val events = _events.receiveAsFlow()

    // The full rows for the current project, kept alongside the counts shown in state so an
    // export tap doesn't need to re-fetch anything.
    private var snapshot: ProjectExportSnapshot? = null

    // Once the self-delete membership write lands, this screen's own Firestore listener loses
    // isMember() and briefly emits PERMISSION_DENIED before the auth-null navigates away — same
    // transient-error class ProfileViewModel's isSigningOut guards against.
    private var isDeletingAccount = false

    init {
        viewModelScope.launch {
            getProjectExport().collect { result ->
                result
                    .onSuccess { data ->
                        snapshot = data
                        _state.update {
                            it.copy(
                                projectName = data.projectName,
                                taskCount = data.taskCount,
                                handoffCount = data.handoffCount,
                                memberCount = data.memberCount,
                                isLoading = false
                            )
                        }
                    }
                if (result is Result.Error && !isDeletingAccount) {
                    _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
                }
            }
        }
    }

    fun onAction(action: YourDataAction) {
        when (action) {
            is YourDataAction.OnFormatSelected -> _state.update { it.copy(exportFormat = action.format) }
            YourDataAction.OnExportClick -> onExportClick()
            YourDataAction.OnDeleteAccountClick -> _state.update { it.copy(showDeleteConfirm = true) }
            YourDataAction.OnDismissDeleteConfirm -> _state.update { it.copy(showDeleteConfirm = false) }
            YourDataAction.OnConfirmDeleteClick -> onConfirmDeleteClick()
            YourDataAction.OnErrorDismissed -> _state.update { it.copy(error = null) }
            YourDataAction.OnBackClick -> viewModelScope.launch { _events.send(YourDataEvent.NavigateBack) }
        }
    }

    private fun onExportClick() {
        val current = snapshot ?: return
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            when (val result = exportProjectData(current.projectName, current, _state.value.exportFormat)) {
                is Result.Success -> _events.send(YourDataEvent.ShareExportFile(result.data))
                is Result.Error -> _state.update { it.copy(error = result.error.toUiText()) }
            }
            _state.update { it.copy(isExporting = false) }
        }
    }

    private fun onConfirmDeleteClick() {
        isDeletingAccount = true
        viewModelScope.launch {
            _state.update { it.copy(showDeleteConfirm = false, isDeletingAccount = true) }
            when (val result = deleteAccount()) {
                DeleteAccountResult.Success -> _events.send(YourDataEvent.AccountDeleted)
                is DeleteAccountResult.BlockedByLeadership -> {
                    isDeletingAccount = false
                    _state.update {
                        it.copy(
                            isDeletingAccount = false,
                            error = UiText.DynamicString(
                                "You're the Leader of ${result.projectNames.joinToString()}. Run Succession there first."
                            )
                        )
                    }
                }
                is DeleteAccountResult.Failed -> {
                    isDeletingAccount = false
                    _state.update { it.copy(isDeletingAccount = false, error = result.error.toUiText()) }
                }
            }
        }
    }
}
