package com.example.temacker

import android.app.Application
import com.example.temacker.core.di.coreModule
import com.example.temacker.feature_auth.di.authModule
import com.example.temacker.feature_profile.di.profileModule
import com.example.temacker.feature_project.di.projectModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                coreModule,
                authModule,
                projectModule,
                profileModule
            )
        }
    }
}
