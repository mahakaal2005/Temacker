package com.example.temacker.feature_tasks.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.temacker.core.domain.network.ConnectivityObserver
import com.example.temacker.core.domain.util.DataError
import com.example.temacker.core.domain.util.Result
import com.example.temacker.feature_tasks.domain.model.EXTRA_HANDOFF_ID
import com.example.temacker.feature_tasks.domain.model.EXTRA_PROJECT_ID
import com.example.temacker.feature_tasks.domain.model.EXTRA_TASK_ID
import com.example.temacker.feature_tasks.domain.use_case.AcceptHandoffUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Handles the inline Accept button; Decline opens the app because it needs a reason.
class HandoffActionReceiver : BroadcastReceiver(), KoinComponent {
    private val acceptHandoff: AcceptHandoffUseCase by inject()
    private val connectivity: ConnectivityObserver by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ACCEPT) return
        val projectId = intent.getStringExtra(EXTRA_PROJECT_ID) ?: return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val handoffId = intent.getStringExtra(EXTRA_HANDOFF_ID) ?: return
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                val offline = !connectivity.isOnline.first()
                when (val result = acceptHandoff(projectId, taskId, handoffId)) {
                    is Result.Success -> {
                        HandoffNotificationFactory.cancel(appContext, handoffId)
                        if (offline) withContext(Dispatchers.Main) {
                            Toast.makeText(appContext, "Saved on this phone. It sends when you're back online.", Toast.LENGTH_LONG).show()
                        }
                    }
                    is Result.Error -> withContext(Dispatchers.Main) {
                        Toast.makeText(appContext, failureMessage(result.error), Toast.LENGTH_LONG).show()
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun failureMessage(error: DataError) = when (error) {
        DataError.Network.CONFLICT -> "That handoff has already changed. Open Temacker to see where it stands."
        else -> "Couldn't accept. Open Temacker to try again."
    }

    companion object {
        const val ACTION_ACCEPT = "com.example.temacker.action.ACCEPT_HANDOFF"
    }
}
