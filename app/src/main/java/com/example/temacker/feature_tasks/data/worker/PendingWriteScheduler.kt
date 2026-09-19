package com.example.temacker.feature_tasks.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

interface PendingWriteScheduler {
    fun enqueue()
}

class WorkManagerPendingWriteScheduler(private val context: Context) : PendingWriteScheduler {
    override fun enqueue() {
        val request = OneTimeWorkRequestBuilder<PendingWriteWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        // Appending keeps rows queued mid-drain from being missed by the run already in flight.
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    private companion object {
        const val WORK_NAME = "pending-writes-drain"
    }
}
