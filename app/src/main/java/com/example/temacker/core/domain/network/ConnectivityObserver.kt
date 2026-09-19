package com.example.temacker.core.domain.network

import kotlinx.coroutines.flow.Flow

// Cross-feature contract (architecture §8) — impl lives in core/data, bound in core/di.
interface ConnectivityObserver {
    // True while the default network has validated internet access.
    val isOnline: Flow<Boolean>
}
