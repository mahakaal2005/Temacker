package com.example.temacker.feature_tasks.data.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.temacker.R
import com.example.temacker.core.presentation.components.initialsOf
import com.example.temacker.feature_tasks.domain.model.EXTRA_DESTINATION
import com.example.temacker.feature_tasks.domain.model.EXTRA_HANDOFF_ID
import com.example.temacker.feature_tasks.domain.model.EXTRA_POSTED_AT
import com.example.temacker.feature_tasks.domain.model.EXTRA_PROJECT_ID
import com.example.temacker.feature_tasks.domain.model.EXTRA_TASK_ID
import com.example.temacker.feature_tasks.domain.model.HandoffDestination

private const val NOTIFICATION_ID = 1
private const val GROUP_SUMMARY_ID = 2
private const val AMBER = 0xFFE8A13A.toInt()
private const val CORAL = 0xFFB32C45.toInt()
// Mirrors core/presentation/designsystem/Color.kt's TealWash/TealInk (Avatar's default NEUTRAL
// tone) — kept as plain ARGB ints here rather than importing Compose Color into a data-layer file.
private const val AVATAR_WASH = 0xFFE6F8F4.toInt()
private const val AVATAR_INK = 0xFF0D7060.toInt()
private const val PI_FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

object HandoffNotificationFactory {

    @SuppressLint("MissingPermission")
    fun show(context: Context, push: HandoffPush) {
        val manager = NotificationManagerCompat.from(context)
        if (!canPost(context, manager)) return
        // Tagged by handoff so a later push about the same handoff replaces this one.
        manager.notify(push.handoffId, NOTIFICATION_ID, build(context, push))
        // Groups every notification for a project under one shade entry when several offers stack up.
        if (push.projectId.isNotBlank()) {
            manager.notify(push.projectId, GROUP_SUMMARY_ID, groupSummary(context, push))
        }
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

    // "Still waiting" nudges use coral + a clock glyph; every other push keeps the amber baton.
    private fun isWaitingNudge(push: HandoffPush) =
        push.type == HandoffPushType.NUDGE_OFFERER || push.type == HandoffPushType.NUDGE_RECIPIENT

    private fun smallIconRes(push: HandoffPush) = if (isWaitingNudge(push)) R.drawable.ic_stat_waiting else R.drawable.ic_stat_baton
    private fun accentColor(push: HandoffPush) = if (isWaitingNudge(push)) CORAL else AMBER

    private fun build(context: Context, push: HandoffPush) =
        NotificationCompat.Builder(context, HandoffNotificationChannel.ID)
            .setSmallIcon(smallIconRes(push))
            .setColor(accentColor(push))
            .setContentTitle(push.title)
            .setContentText(push.text)
            // Which project this is about; the public lock-screen version below never shows it.
            .apply { if (push.projectName.isNotBlank()) setSubText(push.projectName) }
            .apply { if (push.actorName.isNotBlank()) setLargeIcon(actorIcon(push.actorName)) }
            .setStyle(NotificationCompat.BigTextStyle().bigText(push.text))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion(context))
            .setAutoCancel(true)
            .apply { if (push.projectId.isNotBlank()) setGroup(push.projectId) }
            .setContentIntent(launchIntent(context, push, push.tapDestination, requestOffset = 0))
            .apply { if (push.hasActions) addActions(context, push) }
            .build()

    // One summary notification per project — several stacked offers collapse under it in the shade.
    private fun groupSummary(context: Context, push: HandoffPush) =
        NotificationCompat.Builder(context, HandoffNotificationChannel.ID)
            .setSmallIcon(R.drawable.ic_stat_baton)
            .setColor(AMBER)
            .setContentTitle(push.projectName.ifBlank { PUBLIC_TITLE })
            .setGroup(push.projectId)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

    // Renders the same initials Avatar shows in-app onto a wash-colored circle bitmap for the
    // notification's large icon.
    private fun actorIcon(name: String): Bitmap {
        val sizePx = 128
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AVATAR_WASH }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, circlePaint)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AVATAR_INK
            textSize = sizePx * 0.4f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val text = initialsOf(name)
        val textY = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(text, sizePx / 2f, textY, textPaint)
        return bitmap
    }

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
        addAction(R.drawable.ic_check_small, "Accept", acceptIntent)
        addAction(R.drawable.ic_close_small, "Decline", launchIntent(context, push, HandoffDestination.DECLINE, requestOffset = 2))
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
