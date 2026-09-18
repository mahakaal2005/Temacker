package com.example.temacker.feature_project.domain.use_case

import com.example.temacker.core.domain.repository.TeamInsightsProvider

class ObservePulseUseCase(
    private val teamInsightsProvider: TeamInsightsProvider
) {
    operator fun invoke(projectId: String, limit: Int = 50) = teamInsightsProvider.observePulse(projectId, limit)
}
