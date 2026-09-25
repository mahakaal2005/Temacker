package com.example.temacker.core.domain.repository

import kotlinx.coroutines.flow.Flow

// Cross-feature contract (architecture §8, seventh contract) — which of the user's projects the app
// currently shows. Local-only (DataStore), never synced. Impl lives in feature_project/data, bound in
// feature_project/di/ProjectModule.kt.
interface SelectedProjectStore {
    fun observeSelectedProjectId(): Flow<String?>
    suspend fun setSelectedProjectId(id: String)
    suspend fun clear()
}
