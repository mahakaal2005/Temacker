package com.example.temacker.feature_profile.presentation.your_data

sealed interface YourDataEvent {
    data class ShareExportFile(val filePath: String) : YourDataEvent
    data object NavigateBack : YourDataEvent
    data object AccountDeleted : YourDataEvent
}
