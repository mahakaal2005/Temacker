package com.example.temacker.feature_auth.domain.use_case

import com.example.temacker.core.domain.session.SessionManager
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase(
    private val sessionManager: SessionManager
) {
    operator fun invoke(): Flow<Boolean> = sessionManager.isLoggedIn()
}
