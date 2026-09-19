package com.example.temacker.feature_tasks.data.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.temacker.R
import com.example.temacker.feature_tasks.domain.model.EXTRA_DESTINATION
import com.example.temacker.feature_tasks.domain.model.EXTRA_HANDOFF_ID
import com.example.temacker.feature_tasks.domain.model.EXTRA_POSTED_AT
import com.example.temacker.feature_tasks.domain.model.EXTRA_PROJECT_ID
import com.example.temacker.feature_tasks.domain.model.EXTRA_TASK_ID
import com.example.temacker.feature_tasks.domain.model.HandoffDestination

private const val NOTIFICATION_ID = 1
private const val AMBER = 0xFFE8A13A.toInt()
private const val PI_FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

object HandoffNotificationFactory {

    @SuppressLint("MissingPermission")
    fun show(context: Context, push: HandoffPush) {
        val manager = NotificationManagerCompat.from(context)
        if (!canPost(context, manager)) return
        // Tagged by handoff so a later push about the same handoff replaces this one.
        manager.notify(push.handoffId, NOTIFICATION_ID, build(context, push))
    }

    fun cancel(context: Context, handoffId: String) {
        NotificationManagerCompat.from(context).cancel(handoffId, NOTIFICATION_ID)
    }

    private fun canPost(context: Context, manager: NotificationManagerCompat): Boolean {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return granted && manager.areNotificationsEnabled()
    }

    private fun build(context: Context, push: HandoffPush) =
        NotificationCompat.Builder(context, HandoffNotificationChannel.ID)
            .setSmallIcon(R.drawable.ic_stat_baton)
            .setColor(AMBER)
            .setContentTitle(push.title)
            .setContentText(push.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(push.text))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion(context))
            .setAutoCancel(true)
            .setContentIntent(launchIntent(context, push, push.tapDestination, requestOffset = 0))
            .apply { if (push.hasActions) addActions(context, push) }
            .build()

    // What a locked screen shows when the user hides sensitive content.
    private fun publicVersion(context: Context) =
        NotificationCompat.Builder(context, HandoffNotificationChannel.ID)
            .setSmallIcon(R.drawable.ic_stat_baton)
            .setColor(AMBER)
            .setContentTitle(PUBLIC_TITLE)
            .setContentText(PUBLIC_TEXT)
            .build()

    private fun NotificationCompat.Builder.addActions(context: Context, push: HandoffPush) {
        val accept = Intent(context, HandoffActionReceiver::class.java).apply {
            action = HandoffActionReceiver.ACTION_ACCEPT
            putExtra(EXTRA_PROJECT_ID, push.projectId)
            putExtra(EXTRA_TASK_ID, push.taskId)
            putExtra(EXTRA_HANDOFF_ID, push.handoffId)
        }
        val acceptIntent = PendingIntent.getBroadcast(context, requestCode(push, 1), accept, PI_FLAGS)
        addAction(0, "Accept", acceptIntent)
        addAction(0, "Decline", launchIntent(context, push, HandoffDestination.DECLINE, requestOffset = 2))
    }

    // Built from the launcher intent so feature_tasks never imports MainActivity.
    private fun launchIntent(context: Context, push: HandoffPush, destination: HandoffDestination, requestOffset: Int) =
        PendingIntent.getActivity(
            context,
            requestCode(push, requestOffset),
            context.packageManager.getLaunchIntentForPackage(context.packageName)!!.apply {
                putExtra(EXTRA_DESTINATION, destination.name)
                putExtra(EXTRA_PROJECT_ID, push.projectId)
                putExtra(EXTRA_POSTED_AT, System.nanoTime())
                putExtra(EXTRA_TASK_ID, push.taskId)
                putExtra(EXTRA_HANDOFF_ID, push.handoffId)
            },
            PI_FLAGS
        )

    // PendingIntents match on everything except extras, so each handoff+action needs its own code.
    private fun requestCode(push: HandoffPush, offset: Int) = push.handoffId.hashCode() * 4 + offset
}
