package com.example.temacker.feature_profile.di

import com.example.temacker.feature_profile.data.repository.FileExportWriter
import com.example.temacker.feature_profile.domain.repository.ExportFileWriter
import com.example.temacker.feature_profile.domain.use_case.DeleteAccountUseCase
import com.example.temacker.feature_profile.domain.use_case.ExportProjectDataUseCase
import com.example.temacker.feature_profile.domain.use_case.GetProjectExportUseCase
import com.example.temacker.feature_profile.presentation.plan_limits.PlanLimitsViewModel
import com.example.temacker.feature_profile.presentation.profile.ProfileViewModel
import com.example.temacker.feature_profile.presentation.your_data.YourDataViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
    single<ExportFileWriter> { FileExportWriter(androidContext()) }

    factoryOf(::GetProjectExportUseCase)
    factoryOf(::ExportProjectDataUseCase)
    factoryOf(::DeleteAccountUseCase)

    viewModelOf(::ProfileViewModel)
    viewModelOf(::YourDataViewModel)
    viewModelOf(::PlanLimitsViewModel)
}
