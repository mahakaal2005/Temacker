package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

// Counts handoffs waiting on the current user across every project they belong to, so the badge
// is always actionable no matter which project is selected.
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
            currentProjectProvider.observeUserProjectRefs()
                // A transient error keeps the last badge value instead of flashing to zero.
                .mapNotNull { (it as? Result.Success)?.data?.map { project -> project.id } }
                .distinctUntilChanged()
                .flatMapLatest { projectIds ->
                    if (projectIds.isEmpty()) flowOf(0)
                    else combine(
                        projectIds.map { id ->
                            taskRepository.observePendingHandoffs(id).mapNotNull { (it as? Result.Success)?.data?.size }
                        }
                    ) { counts -> counts.sum() }
                }
        }
    }
}
