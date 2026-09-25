package com.example.temacker.feature_tasks.presentation.notification_rationale

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.rememberAppHaptics
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationRationaleRoot(
    onClose: () -> Unit,
    viewModel: NotificationRationaleViewModel = koinViewModel()
) {
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onAction(NotificationRationaleAction.OnPermissionResult)
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NotificationRationaleEvent.RequestPermission ->
                // Below API 33 there is no runtime permission, so there is nothing to ask.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onClose()
                }
            NotificationRationaleEvent.Close -> onClose()
        }
    }

    NotificationRationaleScreen(onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationRationaleScreen(onAction: (NotificationRationaleAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("Notifications") },
            navigationIcon = {
                IconButton(onClick = { onAction(NotificationRationaleAction.OnNotNowClick) }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 22.dp)) {
            Text(
                "Temacker only pings you when a baton moves",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                "Three things, and no digests, streaks or reminders to open the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink500,
                modifier = Modifier.padding(top = 10.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Line)
            ) {
                StaggerIn(index = 0) {
                    RationaleRow(Icons.Rounded.Notifications, AmberWash, AmberInk, "Someone offers you a task", "Accept or decline from the notification")
                }
                StaggerIn(index = 1) {
                    RationaleRow(Icons.Rounded.Check, TealWash, TealInk, "Yours is accepted or declined", "With the reason, if it was declined")
                }
                StaggerIn(index = 2) {
                    RationaleRow(Icons.Rounded.Schedule, NeutralWash, Ink500, "Your offer goes unanswered", "After 18 hours · both sides get it")
                }
            }
        }
        Column(modifier = Modifier.fillMaxWidth().padding(22.dp)) {
            val haptics = rememberAppHaptics()
            Button(
                onClick = {
                    haptics.confirm()
                    onAction(NotificationRationaleAction.OnTurnOnClick)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Turn on notifications") }
            TextButton(
                onClick = { onAction(NotificationRationaleAction.OnNotNowClick) },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            ) { Text("Not now") }
        }
    }
}

@Composable
private fun StaggerIn(index: Int, content: @Composable () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    var visible by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(reducedMotion) { if (!reducedMotion) visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(220, delayMillis = index * 80),
        label = "rationaleRowStagger"
    )
    Column(modifier = Modifier.alpha(alpha)) { content() }
}

@Composable
private fun RationaleRow(icon: ImageVector, wash: Color, tint: Color, title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(36.dp).background(wash, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = Ink500)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationRationaleScreenPreview() {
    TemackerTheme { NotificationRationaleScreen(onAction = {}) }
}
