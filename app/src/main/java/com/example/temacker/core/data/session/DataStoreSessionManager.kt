package com.example.temacker.core.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.temacker.core.domain.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreSessionManager(
    private val dataStore: DataStore<Preferences>
) : SessionManager {

    override fun isLoggedIn(): Flow<Boolean> = dataStore.data.map { it[UID_KEY] != null }

    override suspend fun getUid(): String? = dataStore.data.first()[UID_KEY]

    override suspend fun setSession(uid: String?) {
        dataStore.edit { prefs ->
            if (uid == null) prefs.remove(UID_KEY) else prefs[UID_KEY] = uid
        }
    }

    companion object {
        private val UID_KEY = stringPreferencesKey("session_uid")
    }
}
