package com.example.temacker.feature_profile.presentation.your_data

import com.example.temacker.feature_profile.domain.model.ExportFormat

sealed interface YourDataAction {
    data class OnFormatSelected(val format: ExportFormat) : YourDataAction
    data object OnExportClick : YourDataAction
    data object OnDeleteAccountClick : YourDataAction
    data object OnConfirmDeleteClick : YourDataAction
    data object OnDismissDeleteConfirm : YourDataAction
    data object OnErrorDismissed : YourDataAction
    data object OnBackClick : YourDataAction
}
