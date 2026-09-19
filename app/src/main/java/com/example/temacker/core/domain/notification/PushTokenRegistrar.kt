package com.example.temacker.core.domain.notification

// Cross-feature contract (architecture §8) — impl lives in core/data, bound in core/di.
interface PushTokenRegistrar {
    suspend fun register()
    suspend fun onNewToken(token: String)
    suspend fun unregister()
}
