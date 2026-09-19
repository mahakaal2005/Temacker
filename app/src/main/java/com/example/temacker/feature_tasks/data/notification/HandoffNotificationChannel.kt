package com.example.temacker.feature_tasks.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object HandoffNotificationChannel {
    const val ID = "handoffs"

    fun create(context: Context) {
        // Channels only exist on API 26+; older versions ignore importance per channel.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(ID, "Handoffs", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "A baton offered to you, accepted, declined, or left unanswered."
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
