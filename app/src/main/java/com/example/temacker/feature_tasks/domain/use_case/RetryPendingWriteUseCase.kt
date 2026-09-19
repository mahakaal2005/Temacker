package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.PendingWriteRepository

class RetryPendingWriteUseCase(
    private val repository: PendingWriteRepository
) {
    suspend operator fun invoke(id: Long) = repository.retry(id)
}
