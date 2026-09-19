package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.NotificationRationaleRepository

class ObserveNotificationRationaleSeenUseCase(
    private val repository: NotificationRationaleRepository
) {
    operator fun invoke() = repository.hasSeenRationale()
}
