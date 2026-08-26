package com.example.temacker.feature_auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.feature_auth.domain.use_case.ObserveSessionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val observeSession: ObserveSessionUseCase
) : ViewModel() {

    private val _events = Channel<SplashEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            // No project-membership check yet — that arrives with feature_project.
            val isLoggedIn = observeSession().first()
            _events.send(if (isLoggedIn) SplashEvent.NavigateToApp else SplashEvent.NavigateToLogin)
        }
    }
}
