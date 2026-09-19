package com.example.temacker

import android.app.Application
import com.example.temacker.core.di.coreModule
import com.example.temacker.core.domain.notification.PushTokenRegistrar
import com.example.temacker.core.domain.session.SessionManager
import com.example.temacker.feature_auth.di.authModule
import com.example.temacker.feature_profile.di.profileModule
import com.example.temacker.feature_project.di.projectModule
import com.example.temacker.feature_tasks.data.notification.HandoffNotificationChannel
import com.example.temacker.feature_tasks.di.tasksModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                coreModule,
                authModule,
                projectModule,
                tasksModule,
                profileModule
            )
        }
        HandoffNotificationChannel.create(this)
        registerPushTokenOnLogin()
    }

    // Covers both fresh sign-ins and users who were already signed in when the app launched.
    private fun registerPushTokenOnLogin() {
        val registrar = get<PushTokenRegistrar>()
        appScope.launch {
            get<SessionManager>().isLoggedIn().distinctUntilChanged().filter { it }.collect { registrar.register() }
        }
    }
}
