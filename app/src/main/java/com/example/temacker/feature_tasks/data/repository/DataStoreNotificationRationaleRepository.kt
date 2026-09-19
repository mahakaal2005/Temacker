package com.example.temacker.feature_tasks.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.temacker.feature_tasks.domain.repository.NotificationRationaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreNotificationRationaleRepository(
    private val dataStore: DataStore<Preferences>
) : NotificationRationaleRepository {

    override fun hasSeenRationale(): Flow<Boolean> = dataStore.data.map { it[SEEN_KEY] == true }

    override suspend fun markRationaleSeen() {
        dataStore.edit { it[SEEN_KEY] = true }
    }

    private companion object {
        val SEEN_KEY = booleanPreferencesKey("notification_rationale_seen")
    }
}
