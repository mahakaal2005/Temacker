package com.example.temacker.feature_auth.di

import com.example.temacker.feature_auth.data.repository.InMemoryAuthRepository
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import com.example.temacker.feature_auth.domain.use_case.LoginUseCase
import com.example.temacker.feature_auth.domain.use_case.ObserveSessionUseCase
import com.example.temacker.feature_auth.presentation.login.LoginViewModel
import com.example.temacker.feature_auth.presentation.splash.SplashViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// InMemoryAuthRepository today; swap to a Firebase-backed OfflineFirstAuthRepository once
// google-services.json is available — everything above this binding is untouched.
val authModule = module {
    singleOf(::InMemoryAuthRepository) { bind<AuthRepository>() }

    factoryOf(::LoginUseCase)
    factoryOf(::ObserveSessionUseCase)

    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
}
