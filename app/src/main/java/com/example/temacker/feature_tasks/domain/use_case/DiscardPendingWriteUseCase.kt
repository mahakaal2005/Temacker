package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.feature_tasks.domain.repository.PendingWriteRepository

class DiscardPendingWriteUseCase(
    private val repository: PendingWriteRepository
) {
    suspend operator fun invoke(id: Long) = repository.discard(id)
}
