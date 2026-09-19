package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

// Counts only handoffs waiting on the current user, so the badge is always actionable.
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveInboxBadgeCountUseCase(
    private val sessionManager: SessionManager,
    private val currentProjectProvider: CurrentProjectProvider,
    private val taskRepository: TaskRepository
) {
    // Keyed on login state so signing out and in as someone else restarts the uid-scoped query.
    operator fun invoke(): Flow<Int> = sessionManager.isLoggedIn().distinctUntilChanged().flatMapLatest { loggedIn ->
        if (!loggedIn) {
            flowOf(0)
        } else {
            currentProjectProvider.observeCurrentProjectId()
                .filter { it is Result.Success }
                .map { (it as Result.Success).data }
                .distinctUntilChanged()
                .flatMapLatest { projectId ->
                    if (projectId == null) flowOf(0)
                    else taskRepository.observePendingHandoffs(projectId).mapNotNull { (it as? Result.Success)?.data?.size }
                }
        }
    }
}
