package com.example.temacker.core.data.database

import com.example.temacker.core.domain.repository.LocalDataCleaner
import com.example.temacker.core.domain.repository.SelectedProjectStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomLocalDataCleaner(
    private val appDatabase: AppDatabase,
    private val selectedProjectStore: SelectedProjectStore
) : LocalDataCleaner {

    override suspend fun clearAll() {
        // clearAllTables() blocks, so keep it off the main thread.
        withContext(Dispatchers.IO) { appDatabase.clearAllTables() }
        selectedProjectStore.clear()
    }
}
