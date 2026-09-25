package com.example.temacker.feature_tasks.di

import com.example.temacker.core.domain.repository.ExportDataProvider
import com.example.temacker.core.domain.repository.TeamInsightsProvider
import com.example.temacker.feature_tasks.data.remote.FirestoreTaskRemoteDataSource
import com.example.temacker.feature_tasks.data.remote.TaskRemoteDataSource
import com.example.temacker.feature_tasks.data.repository.OfflineFirstPendingWriteRepository
import com.example.temacker.feature_tasks.data.repository.OfflineFirstTaskRepository
import com.example.temacker.feature_tasks.data.repository.TaskExportDataProvider
import com.example.temacker.feature_tasks.data.repository.TaskTeamInsightsProvider
import com.example.temacker.feature_tasks.data.worker.PendingWriteReplayer
import com.example.temacker.feature_tasks.data.worker.PendingWriteScheduler
import com.example.temacker.feature_tasks.data.worker.WorkManagerPendingWriteScheduler
import com.example.temacker.feature_tasks.domain.repository.PendingWriteRepository
import com.example.temacker.feature_tasks.domain.repository.TaskRepository
import com.example.temacker.feature_tasks.domain.use_case.DiscardPendingWriteUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveConnectivityUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObservePendingWritesUseCase
import com.example.temacker.feature_tasks.domain.use_case.RetryPendingWriteUseCase
import org.koin.android.ext.koin.androidContext
import com.example.temacker.feature_tasks.domain.use_case.AcceptHandoffUseCase
import com.example.temacker.feature_tasks.domain.use_case.CreateTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.DeclineHandoffUseCase
import com.example.temacker.feature_tasks.domain.use_case.DeleteTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.MarkTaskDoneUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveBoardUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectIdUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectSummaryUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveCurrentProjectMemberUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveHandoffTrailUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveInboxBadgeCountUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveInboxUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveOtherProjectQueueCountUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveOtherProjectsWaitingUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObservePendingHandoffsUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveProjectMembersUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveTaskUseCase
import com.example.temacker.feature_tasks.domain.use_case.OfferHandoffUseCase
import com.example.temacker.feature_tasks.presentation.board.BoardViewModel
import com.example.temacker.feature_tasks.presentation.decline.DeclineViewModel
import com.example.temacker.feature_tasks.presentation.handoff.HandoffViewModel
import com.example.temacker.feature_tasks.presentation.inbox.InboxBadgeViewModel
import com.example.temacker.feature_tasks.presentation.inbox.InboxViewModel
import com.example.temacker.feature_tasks.presentation.incoming.IncomingViewModel
import com.example.temacker.feature_tasks.presentation.new_task.NewTaskViewModel
import com.example.temacker.feature_tasks.presentation.queue.QueueViewModel
import com.example.temacker.feature_tasks.presentation.task_detail.TaskDetailViewModel
import org.koin.core.module.dsl.bind
import com.example.temacker.feature_tasks.data.repository.DataStoreNotificationRationaleRepository
import com.example.temacker.feature_tasks.domain.repository.NotificationRationaleRepository
import com.example.temacker.feature_tasks.domain.use_case.MarkNotificationRationaleSeenUseCase
import com.example.temacker.feature_tasks.domain.use_case.ObserveNotificationRationaleSeenUseCase
import com.example.temacker.feature_tasks.presentation.notification_rationale.NotificationRationaleGateViewModel
import com.example.temacker.feature_tasks.presentation.notification_rationale.NotificationRationaleViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val tasksModule = module {
    singleOf(::FirestoreTaskRemoteDataSource) { bind<TaskRemoteDataSource>() }
    singleOf(::OfflineFirstTaskRepository) { bind<TaskRepository>() }
    singleOf(::OfflineFirstPendingWriteRepository) { bind<PendingWriteRepository>() }
    single<PendingWriteScheduler> { WorkManagerPendingWriteScheduler(androidContext()) }
    singleOf(::PendingWriteReplayer)
    singleOf(::DataStoreNotificationRationaleRepository) { bind<NotificationRationaleRepository>() }
    singleOf(::TaskTeamInsightsProvider) { bind<TeamInsightsProvider>() }
    singleOf(::TaskExportDataProvider) { bind<ExportDataProvider>() }

    factoryOf(::ObserveCurrentProjectIdUseCase)
    factoryOf(::ObserveCurrentProjectSummaryUseCase)
    factoryOf(::ObserveConnectivityUseCase)
    factoryOf(::ObservePendingWritesUseCase)
    factoryOf(::RetryPendingWriteUseCase)
    factoryOf(::DiscardPendingWriteUseCase)
    factoryOf(::ObserveCurrentProjectMemberUseCase)
    factoryOf(::ObserveProjectMembersUseCase)
    factoryOf(::ObserveBoardUseCase)
    factoryOf(::ObserveTaskUseCase)
    factoryOf(::ObserveHandoffTrailUseCase)
    factoryOf(::ObservePendingHandoffsUseCase)
    factoryOf(::ObserveInboxUseCase)
    factoryOf(::ObserveInboxBadgeCountUseCase)
    factoryOf(::ObserveOtherProjectsWaitingUseCase)
    factoryOf(::ObserveOtherProjectQueueCountUseCase)
    factoryOf(::ObserveNotificationRationaleSeenUseCase)
    factoryOf(::MarkNotificationRationaleSeenUseCase)
    factoryOf(::CreateTaskUseCase)
    factoryOf(::OfferHandoffUseCase)
    factoryOf(::AcceptHandoffUseCase)
    factoryOf(::DeclineHandoffUseCase)
    factoryOf(::MarkTaskDoneUseCase)
    factoryOf(::DeleteTaskUseCase)

    viewModelOf(::BoardViewModel)
    viewModelOf(::InboxViewModel)
    viewModelOf(::QueueViewModel)
    viewModelOf(::InboxBadgeViewModel)
    viewModelOf(::NotificationRationaleGateViewModel)
    viewModelOf(::NotificationRationaleViewModel)
    viewModelOf(::NewTaskViewModel)
    // taskId/handoffId are runtime nav params, not resolvable by constructor reference alone —
    // lambda form is the documented fallback (android-di-koin skill).
    viewModel { (taskId: String) -> TaskDetailViewModel(taskId, get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (taskId: String) -> HandoffViewModel(taskId, get(), get(), get(), get()) }
    viewModel { (taskId: String, handoffId: String) -> IncomingViewModel(taskId, handoffId, get(), get(), get(), get()) }
    viewModel { (taskId: String, handoffId: String) -> DeclineViewModel(taskId, handoffId, get(), get()) }
}
