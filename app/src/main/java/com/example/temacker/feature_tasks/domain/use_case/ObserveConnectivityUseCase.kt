package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.network.ConnectivityObserver

class ObserveConnectivityUseCase(
    private val connectivityObserver: ConnectivityObserver
) {
    operator fun invoke() = connectivityObserver.isOnline
}
