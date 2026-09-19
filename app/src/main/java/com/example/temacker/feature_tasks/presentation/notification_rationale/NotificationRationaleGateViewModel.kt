package com.example.temacker.feature_tasks.presentation.notification_rationale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.feature_tasks.domain.use_case.ObserveNotificationRationaleSeenUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Hosted at the app shell; null until DataStore answers so the gate never fires on a guess.
class NotificationRationaleGateViewModel(
    observeSeen: ObserveNotificationRationaleSeenUseCase
) : ViewModel() {
    val seen: StateFlow<Boolean?> = observeSeen()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
