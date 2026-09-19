package com.example.temacker.feature_tasks.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PendingWriteWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {
    private val replayer: PendingWriteReplayer by inject()

    override suspend fun doWork(): Result = try {
        // A batch write can hang offline, so a stuck run times out and retries.
        when (withTimeout(RUN_TIMEOUT_MS) { replayer.drain() }) {
            DrainOutcome.DONE -> Result.success()
            DrainOutcome.RETRY_LATER -> Result.retry()
        }
    } catch (e: TimeoutCancellationException) {
        Result.retry()
    }

    private companion object {
        const val RUN_TIMEOUT_MS = 60_000L
    }
}
