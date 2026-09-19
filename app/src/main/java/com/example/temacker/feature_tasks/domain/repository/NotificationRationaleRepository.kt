package com.example.temacker.feature_tasks.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificationRationaleRepository {
    fun hasSeenRationale(): Flow<Boolean>
    suspend fun markRationaleSeen()
}
