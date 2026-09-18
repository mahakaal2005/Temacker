package com.example.temacker.feature_project.di

import com.example.temacker.feature_project.data.remote.FirestoreInviteCodeRemoteDataSource
import com.example.temacker.feature_project.data.remote.FirestoreMembershipRemoteDataSource
import com.example.temacker.feature_project.data.remote.FirestoreProjectRemoteDataSource
import com.example.temacker.feature_project.data.remote.FirestoreRoleRemoteDataSource
import com.example.temacker.feature_project.data.remote.InviteCodeRemoteDataSource
import com.example.temacker.feature_project.data.remote.MembershipRemoteDataSource
import com.example.temacker.feature_project.data.remote.ProjectRemoteDataSource
import com.example.temacker.feature_project.data.remote.RoleRemoteDataSource
import com.example.temacker.core.domain.repository.CurrentProjectProvider
import com.example.temacker.core.domain.repository.ProjectMemberProvider
import com.example.temacker.feature_project.data.repository.MembershipProjectMemberProvider
import com.example.temacker.feature_project.data.repository.ProjectCurrentProjectProvider
import com.example.temacker.feature_project.data.repository.OfflineFirstInviteCodeRepository
import com.example.temacker.feature_project.data.repository.OfflineFirstMembershipRepository
import com.example.temacker.feature_project.data.repository.OfflineFirstProjectRepository
import com.example.temacker.feature_project.data.repository.OfflineFirstRoleRepository
import com.example.temacker.feature_project.domain.repository.InviteCodeRepository
import com.example.temacker.feature_project.domain.repository.MembershipRepository
import com.example.temacker.feature_project.domain.repository.ProjectRepository
import com.example.temacker.feature_project.domain.repository.RoleRepository
import com.example.temacker.feature_project.domain.use_case.CreateProjectUseCase
import com.example.temacker.feature_project.domain.use_case.CreateRoleUseCase
import com.example.temacker.feature_project.domain.use_case.DeleteRoleUseCase
import com.example.temacker.feature_project.domain.use_case.GenerateInviteCodeUseCase
import com.example.temacker.feature_project.domain.use_case.JoinProjectUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveActiveInviteCodeUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveCurrentMembershipUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveMembersUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveProjectUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveRolesUseCase
import com.example.temacker.feature_project.domain.use_case.ObserveUserProjectsUseCase
import com.example.temacker.feature_project.domain.use_case.ReassignMemberRoleUseCase
import com.example.temacker.feature_project.domain.use_case.RemoveMemberUseCase
import com.example.temacker.feature_project.domain.use_case.UpdateRoleUseCase
import com.example.temacker.feature_project.presentation.create_project.CreateProjectViewModel
import com.example.temacker.feature_project.presentation.gate.ProjectGateViewModel
import com.example.temacker.feature_project.presentation.join_project.JoinProjectViewModel
import com.example.temacker.feature_project.presentation.manage_roles.ManageRolesViewModel
import com.example.temacker.feature_project.presentation.no_project.NoProjectViewModel
import com.example.temacker.feature_project.presentation.roster.RosterViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val projectModule = module {
    singleOf(::FirestoreProjectRemoteDataSource) { bind<ProjectRemoteDataSource>() }
    singleOf(::FirestoreRoleRemoteDataSource) { bind<RoleRemoteDataSource>() }
    singleOf(::FirestoreMembershipRemoteDataSource) { bind<MembershipRemoteDataSource>() }
    singleOf(::FirestoreInviteCodeRemoteDataSource) { bind<InviteCodeRemoteDataSource>() }

    singleOf(::OfflineFirstProjectRepository) { bind<ProjectRepository>() }
    singleOf(::OfflineFirstRoleRepository) { bind<RoleRepository>() }
    singleOf(::OfflineFirstMembershipRepository) { bind<MembershipRepository>() }
    singleOf(::OfflineFirstInviteCodeRepository) { bind<InviteCodeRepository>() }
    singleOf(::MembershipProjectMemberProvider) { bind<ProjectMemberProvider>() }
    singleOf(::ProjectCurrentProjectProvider) { bind<CurrentProjectProvider>() }

    factoryOf(::ObserveUserProjectsUseCase)
    factoryOf(::CreateProjectUseCase)
    factoryOf(::ObserveProjectUseCase)
    factoryOf(::ObserveMembersUseCase)
    factoryOf(::ObserveRolesUseCase)
    factoryOf(::ObserveCurrentMembershipUseCase)
    factoryOf(::JoinProjectUseCase)
    factoryOf(::CreateRoleUseCase)
    factoryOf(::UpdateRoleUseCase)
    factoryOf(::DeleteRoleUseCase)
    factoryOf(::RemoveMemberUseCase)
    factoryOf(::ReassignMemberRoleUseCase)
    factoryOf(::GenerateInviteCodeUseCase)
    factoryOf(::ObserveActiveInviteCodeUseCase)

    viewModelOf(::ProjectGateViewModel)
    viewModelOf(::NoProjectViewModel)
    viewModelOf(::CreateProjectViewModel)
    viewModelOf(::JoinProjectViewModel)
    viewModelOf(::RosterViewModel)
    viewModelOf(::ManageRolesViewModel)
}
