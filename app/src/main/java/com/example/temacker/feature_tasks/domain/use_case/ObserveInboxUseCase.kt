package com.example.temacker.feature_tasks.domain.use_case

import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.model.Inbox
import com.example.temacker.feature_tasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveInboxUseCase(
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager
) {
    operator fun invoke(projectId: String): Flow<Result<Inbox, DataError>> =
        taskRepository.observeInbox(projectId).map { result ->
            when (result) {
                is Result.Success -> {
                    val uid = sessionManager.getUid()
                    if (uid == null) Result.Success(Inbox())
                    else Result.Success(Inbox.from(result.data, uid, System.currentTimeMillis()))
                }
                is Result.Error -> result
            }
        }
}
