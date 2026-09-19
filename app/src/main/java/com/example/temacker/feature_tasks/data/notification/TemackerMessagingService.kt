package com.example.temacker.feature_tasks.data.notification

import com.example.temacker.core.domain.notification.PushTokenRegistrar
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TemackerMessagingService : FirebaseMessagingService() {
    private val tokens: PushTokenRegistrar by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onNewToken(token: String) {
        scope.launch { tokens.onNewToken(token) }
    }

    // Data-only pushes always land here (foreground or not), so the app owns the notification.
    override fun onMessageReceived(message: RemoteMessage) {
        val push = parseHandoffPush(message.data) ?: return
        HandoffNotificationFactory.show(this, push)
    }
}
