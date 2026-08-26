package com.example.temacker.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.temacker.core.data.session.DataStoreSessionManager
import com.example.temacker.core.domain.session.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// AppDatabase is added here once the first Room entity exists (Room rejects @Database with no entities).
val coreModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("temacker_prefs")
        }
    }
    singleOf(::DataStoreSessionManager) { bind<SessionManager>() }
}
