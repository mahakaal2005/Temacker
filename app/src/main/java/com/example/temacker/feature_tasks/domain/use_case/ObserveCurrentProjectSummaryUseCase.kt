package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.repository.CurrentProjectProvider

class ObserveCurrentProjectSummaryUseCase(
    private val currentProjectProvider: CurrentProjectProvider
) {
    operator fun invoke() = currentProjectProvider.observeCurrentProjectSummary()
}
