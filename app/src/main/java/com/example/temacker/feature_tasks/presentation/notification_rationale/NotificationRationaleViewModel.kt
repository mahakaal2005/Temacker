package com.example.temacker.feature_tasks.presentation.notification_rationale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.feature_tasks.domain.use_case.MarkNotificationRationaleSeenUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NotificationRationaleViewModel(
    markSeen: MarkNotificationRationaleSeenUseCase
) : ViewModel() {

    private val eventChannel = Channel<NotificationRationaleEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        // Seen the moment it appears, so it never returns even if the user backs out.
        viewModelScope.launch { markSeen() }
    }

    fun onAction(action: NotificationRationaleAction) {
        viewModelScope.launch {
            when (action) {
                NotificationRationaleAction.OnTurnOnClick -> eventChannel.send(NotificationRationaleEvent.RequestPermission)
                NotificationRationaleAction.OnNotNowClick,
                NotificationRationaleAction.OnPermissionResult -> eventChannel.send(NotificationRationaleEvent.Close)
            }
        }
    }
}
