package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.model.ProjectRef
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.model.OtherProjectWaiting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Offers waiting on the user in every project except the selected one, so a handoff in another
// project is never invisible. Projects with nothing waiting are left out.
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveOtherProjectsWaitingUseCase(
    private val currentProjectProvider: CurrentProjectProvider,
    private val observeInbox: ObserveInboxUseCase
) {
    operator fun invoke(): Flow<List<OtherProjectWaiting>> =
        combine(
            currentProjectProvider.observeCurrentProjectId(),
            currentProjectProvider.observeUserProjectRefs()
        ) { idResult, refsResult ->
            // A transient error on either side would misclassify the current project as "other", so skip it.
            if (idResult !is Result.Success || refsResult !is Result.Success) null
            else refsResult.data.filter { it.id != idResult.data }
        }
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { others -> waitingIn(others) }

    private fun waitingIn(projects: List<ProjectRef>): Flow<List<OtherProjectWaiting>> {
        if (projects.isEmpty()) return flowOf(emptyList())
        return combine(
            projects.map { project ->
                observeInbox(project.id).map { result ->
                    val waiting = (result as? Result.Success)?.data?.waitingOnYou.orEmpty()
                    OtherProjectWaiting(project.id, project.name, waiting)
                }
            }
        ) { sections -> sections.filter { it.entries.isNotEmpty() } }
    }
}
