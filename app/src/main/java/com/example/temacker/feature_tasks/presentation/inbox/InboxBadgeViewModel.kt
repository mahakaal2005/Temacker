package com.example.temacker.feature_tasks.presentation.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.feature_tasks.domain.use_case.ObserveInboxBadgeCountUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

// Hosted at the app shell so every bottom-nav screen shows the same badge.
class InboxBadgeViewModel(
    observeInboxBadgeCount: ObserveInboxBadgeCountUseCase
) : ViewModel() {
    val count: StateFlow<Int> = observeInboxBadgeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
