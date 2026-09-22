package com.example.temacker.feature_project.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.temacker.core.domain.repository.SelectedProjectStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSelectedProjectStore(
    private val dataStore: DataStore<Preferences>
) : SelectedProjectStore {

    override fun observeSelectedProjectId(): Flow<String?> = dataStore.data.map { it[SELECTED_PROJECT_ID_KEY] }

    override suspend fun setSelectedProjectId(id: String) {
        dataStore.edit { prefs -> prefs[SELECTED_PROJECT_ID_KEY] = id }
    }

    companion object {
        private val SELECTED_PROJECT_ID_KEY = stringPreferencesKey("selected_project_id")
    }
}
