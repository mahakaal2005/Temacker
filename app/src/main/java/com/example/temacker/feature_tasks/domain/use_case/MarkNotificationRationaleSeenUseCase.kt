package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.NotificationRationaleRepository

class MarkNotificationRationaleSeenUseCase(
    private val repository: NotificationRationaleRepository
) {
    suspend operator fun invoke() = repository.markRationaleSeen()
}
