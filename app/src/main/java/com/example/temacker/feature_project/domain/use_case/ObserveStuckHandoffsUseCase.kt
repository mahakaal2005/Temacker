package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.core.domain.repository.TeamInsightsProvider

class ObserveStuckHandoffsUseCase(
    private val teamInsightsProvider: TeamInsightsProvider
) {
    operator fun invoke(projectId: String) = teamInsightsProvider.observeStuckHandoffs(projectId)
}
