package com.example.temacker.feature_tasks.presentation.import_tasks

import androidx.compose.runtime.Stable
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_tasks.domain.import_tasks.ImportPreview

@Stable
data class ImportTasksState(
    val preview: ImportPreview? = null,
    val isImporting: Boolean = false,
    val error: UiText? = null
)
