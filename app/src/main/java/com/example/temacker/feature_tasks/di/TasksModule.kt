package com.example.temacker.feature_tasks.di

import com.example.temacker.core.domain.repository.TeamInsightsProvider
import com.example.temacker.feature_tasks.data.remote.FirestoreTaskRemoteDataSource
import com.example.temacker.feature_tasks.data.remote.TaskRemoteDataSource
import com.example.temacker.feature_tasks.data.repository.OfflineFirstTaskRepository
import com.example.temacker.feature_tasks.data.repository.TaskTeamInsightsProvider
import com.example.temacker.feature_tasks.domain.repository.TaskRepository
import com.example.temacker.feature_tasks.domain.use_case.AcceptHandoffUseCase
import com.example.temacker.feature_tasks.domain.use_case.CreateTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.DeclineHandoffUseCase
import com.example.temacker.feature_tasks.domain.use_case.DeleteTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.MarkTaskDoneUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveBoardUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectMemberUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveHandoffTrailUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObservePendingHandoffsUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveProjectMembersUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.OfferHandoffUseCase
import com.example.temacker.feature_tasks.presentation.board.BoardViewModel
import com.example.temacker.feature_tasks.presentation.decline.DeclineViewModel
import com.example.temacker.feature_tasks.presentation.handoff.HandoffViewModel
import com.example.temacker.feature_tasks.presentation.incoming.IncomingViewModel
import com.example.temacker.feature_tasks.presentation.new_task.NewTaskViewModel
import com.example.temacker.feature_tasks.presentation.task_detail.TaskDetailViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val tasksModule = module {
    singleOf(::FirestoreTaskRemoteDataSource) { bind<TaskRemoteDataSource>() }
    singleOf(::OfflineFirstTaskRepository) { bind<TaskRepository>() }
    singleOf(::TaskTeamInsightsProvider) { bind<TeamInsightsProvider>() }

    factoryOf(::ObserveCurrentProjectIdUseCase)
    factoryOf(::ObserveCurrentProjectMemberUseCase)
    factoryOf(::ObserveProjectMembersUseCase)
    factoryOf(::ObserveBoardUseCase)
    factoryOf(::ObserveTaskUseCase)
    factoryOf(::ObserveHandoffTrailUseCase)
    factoryOf(::ObservePendingHandoffsUseCase)
    factoryOf(::CreateTaskUseCase)
    factoryOf(::OfferHandoffUseCase)
    factoryOf(::AcceptHandoffUseCase)
    factoryOf(::DeclineHandoffUseCase)
    factoryOf(::MarkTaskDoneUseCase)
    factoryOf(::DeleteTaskUseCase)

    viewModelOf(::BoardViewModel)
    viewModelOf(::NewTaskViewModel)
    // taskId/handoffId are runtime nav params, not resolvable by constructor reference alone —
    // lambda form is the documented fallback (android-di-koin skill).
    viewModel { (taskId: String) -> TaskDetailViewModel(taskId, get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (taskId: String) -> HandoffViewModel(taskId, get(), get(), get(), get()) }
    viewModel { (taskId: String, handoffId: String) -> IncomingViewModel(taskId, handoffId, get(), get(), get(), get()) }
    viewModel { (taskId: String, handoffId: String) -> DeclineViewModel(taskId, handoffId, get(), get()) }
}
