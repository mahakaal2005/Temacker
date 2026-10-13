package com.example.temacker.feature_auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temacker.feature_auth.domain.use_case.ObserveSessionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
            // Hold the splash long enough for its entrance to play even when the session check is instant.
            val startedAt = System.currentTimeMillis()
            val isLoggedIn = observeSession().first()
            delay((MIN_SPLASH_MS - (System.currentTimeMillis() - startedAt)).coerceAtLeast(0L))
            _events.send(if (isLoggedIn) SplashEvent.NavigateToApp else SplashEvent.NavigateToLogin)
        }
    }
}

private const val MIN_SPLASH_MS = 1400L
