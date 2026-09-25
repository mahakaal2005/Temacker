package com.example.temacker.feature_profile.presentation.your_data

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_profile.domain.model.ExportFormat

@Stable
data class YourDataState(
    val projectName: String = "",
    val taskCount: Int = 0,
    val handoffCount: Int = 0,
    val memberCount: Int = 0,
    val exportFormat: ExportFormat = ExportFormat.JSON,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val error: UiText? = null
)
