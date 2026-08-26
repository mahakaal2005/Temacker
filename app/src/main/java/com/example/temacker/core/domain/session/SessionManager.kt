package com.example.temacker.core.domain.session

import kotlinx.coroutines.flow.Flow

// Cross-feature contract (architecture §8) — impl lives in core/data, bound in core/di.
interface SessionManager {
    fun isLoggedIn(): Flow<Boolean>
    suspend fun getUid(): String?
    suspend fun setSession(uid: String?)
}
