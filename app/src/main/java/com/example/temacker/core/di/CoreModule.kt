package com.example.temacker.core.di

import androidx.credentials.CredentialManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.temacker.core.data.activity.CurrentActivityHolder
import com.example.temacker.core.data.database.AppDatabase
import com.example.temacker.core.data.session.DataStoreSessionManager
import com.example.temacker.core.domain.session.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "temacker.db").build()
    }
    single { get<AppDatabase>().projectDao() }
    single { get<AppDatabase>().roleDao() }
    single { get<AppDatabase>().membershipDao() }
    single { get<AppDatabase>().inviteCodeDao() }

    single { FirebaseFirestore.getInstance() }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("temacker_prefs")
        }
    }
    singleOf(::DataStoreSessionManager) { bind<SessionManager>() }

    single { FirebaseAuth.getInstance() }
    single { CredentialManager.create(androidContext()) }

    // createdAtStart: must register its ActivityLifecycleCallbacks before MainActivity is
    // created, not lazily on first Koin injection (which happens after Login is on screen —
    // too late to have caught MainActivity's one and only onActivityResumed call).
    single(createdAtStart = true) { CurrentActivityHolder(androidApplication()) }
}
