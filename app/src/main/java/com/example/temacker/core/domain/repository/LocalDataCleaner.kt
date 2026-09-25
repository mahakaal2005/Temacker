package com.example.temacker.core.domain.repository

// Wipes everything cached on this device for the signed-in user; called on sign-out and account deletion.
interface LocalDataCleaner {
    suspend fun clearAll()
}
