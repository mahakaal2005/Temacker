package com.example.temacker.feature_auth.di

import com.example.temacker.feature_auth.data.remote.AuthRemoteDataSource
import com.example.temacker.feature_auth.data.remote.FirebaseAuthRemoteDataSource
import com.example.temacker.feature_auth.data.repository.OfflineFirstAuthRepository
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import com.example.temacker.feature_auth.domain.use_case.ObserveSessionUseCase
import com.example.temacker.feature_auth.domain.use_case.RegisterWithEmailUseCase
import com.example.temacker.feature_auth.domain.use_case.SignInWithEmailUseCase
import com.example.temacker.feature_auth.domain.use_case.SignInWithGoogleUseCase
import com.example.temacker.feature_auth.presentation.login.LoginViewModel
import com.example.temacker.feature_auth.presentation.splash.SplashViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    singleOf(::FirebaseAuthRemoteDataSource) { bind<AuthRemoteDataSource>() }
    singleOf(::OfflineFirstAuthRepository) { bind<AuthRepository>() }

    factoryOf(::SignInWithGoogleUseCase)
    factoryOf(::SignInWithEmailUseCase)
    factoryOf(::RegisterWithEmailUseCase)
    factoryOf(::ObserveSessionUseCase)

    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
}
