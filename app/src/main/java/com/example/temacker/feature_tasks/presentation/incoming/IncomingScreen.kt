package com.example.temacker.feature_tasks.presentation.incoming

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.Avatar
import com.example.temacker.core.presentation.components.AvatarTone
import com.example.temacker.core.presentation.components.ChipTone
import com.example.temacker.core.presentation.components.EmptyState
import com.example.temacker.core.presentation.components.LoadingState
import com.example.temacker.core.presentation.components.StatusChip
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.components.TmkButtonVariant
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.effectsSpring
import com.example.temacker.core.presentation.designsystem.rememberAppHaptics
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import com.example.temacker.core.presentation.designsystem.spatialDefault
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.feature_tasks.domain.model.Handoff
import com.example.temacker.feature_tasks.domain.model.HandoffStatus
import com.example.temacker.feature_tasks.domain.model.Task
import com.example.temacker.feature_tasks.domain.model.TaskStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncomingRoot(
    taskId: String,
    handoffId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDecline: (String, String) -> Unit,
    viewModel: IncomingViewModel = koinViewModel(parameters = { parametersOf(taskId, handoffId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            IncomingEvent.NavigateBack -> onNavigateBack()
            is IncomingEvent.NavigateToDecline -> onNavigateToDecline(event.taskId, event.handoffId)
        }
    }

    IncomingScreen(state = state, onAction = viewModel::onAction, onNavigateBack = onNavigateBack)
}

// "The whole product is this screen" — the spec's words. Entrance staggers avatar -> headline ->
// note -> card once; the animation never blocks or delays navigation, and a haptic marks the
// decision the moment it's made rather than waiting on the network round-trip.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingScreen(state: IncomingState, onAction: (IncomingAction) -> Unit, onNavigateBack: () -> Unit) {
    val haptics = rememberAppHaptics()
    val reducedMotion = rememberReducedMotion()
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Handoff to you") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(IncomingAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            // Covers a stale notification: the task is gone, or the offer was already answered.
            val notice = state.unavailableNotice()
            if (notice != null) {
                EmptyState(
                    icon = Icons.Rounded.Inbox,
                    title = notice.title,
                    body = notice.detail,
                    primaryLabel = "Back to Inbox",
                    onPrimaryClick = onNavigateBack
                )
            } else if (state.isLoading || state.task == null || state.handoff == null) {
                LoadingState()
            } else {
                val task = state.task
                val handoff = state.handoff
                Column(modifier = Modifier.weight(1f).padding(Spacing.l)) {
                    StaggerIn(index = 0, reducedMotion = reducedMotion) {
                        Avatar(name = handoff.fromDisplayName, size = 56.dp, tone = AvatarTone.NEUTRAL)
                    }
                    StaggerIn(index = 1, reducedMotion = reducedMotion) {
                        Text(
                            "${handoff.fromDisplayName} wants to hand you \"${task.title}\"",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = Spacing.m)
                        )
                    }
                    task.description?.takeIf { it.isNotBlank() }?.let {
                        StaggerIn(index = 2, reducedMotion = reducedMotion) {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink500, modifier = Modifier.padding(top = Spacing.xs))
                        }
                    }
                    task.dueDate?.let {
                        StaggerIn(index = 2, reducedMotion = reducedMotion) {
                            StatusChip(
                                text = "Due ${SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(Date(it))}",
                                tone = ChipTone.NEUTRAL,
                                modifier = Modifier.padding(top = Spacing.s)
                            )
                        }
                    }
                    handoff.note?.let {
                        StaggerIn(index = 3, reducedMotion = reducedMotion) {
                            Text("“$it”", style = MaterialTheme.typography.bodyMedium, color = Ink500, modifier = Modifier.padding(top = Spacing.m))
                        }
                    }
                    StaggerIn(index = 4, reducedMotion = reducedMotion) {
                        Card(modifier = Modifier.fillMaxWidth().padding(top = Spacing.l), colors = CardDefaults.cardColors(containerColor = NeutralWash)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Times handed over", color = Ink500)
                                Text("${task.timesHandedOver}")
                            }
                        }
                    }
                    Text(
                        "Until you accept, this task stays with ${handoff.fromDisplayName} and still counts as theirs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink500,
                        modifier = Modifier.padding(top = Spacing.m)
                    )
                }
                Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(Spacing.l)) {
                    TmkButton(
                        text = "Accept the baton",
                        onClick = { haptics.confirm(); onAction(IncomingAction.OnAcceptClick) },
                        enabled = !state.isResponding,
                        isLoading = state.isResponding,
                        icon = Icons.Rounded.Check
                    )
                    TmkButton(
                        text = "Decline",
                        onClick = { haptics.confirm(); onAction(IncomingAction.OnDeclineClick) },
                        variant = TmkButtonVariant.SECONDARY,
                        enabled = !state.isResponding,
                        icon = Icons.Rounded.Close,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
        }
    }
}

@Composable
private fun StaggerIn(index: Int, reducedMotion: Boolean, content: @Composable () -> Unit) {
    val alpha = remember { Animatable(if (reducedMotion) 1f else 0f) }
    val offsetY = remember { Animatable(if (reducedMotion) 0f else 16f) }
    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            delay(index * 40L)
            launch { alpha.animateTo(1f, animationSpec = effectsSpring()) }
            launch { offsetY.animateTo(0f, animationSpec = spatialDefault()) }
        }
    }
    Box(modifier = Modifier.graphicsLayer { this.alpha = alpha.value; translationY = offsetY.value }) { content() }
}

@Preview(showBackground = true)
@Composable
private fun IncomingScreenPreview() {
    TemackerTheme {
        IncomingScreen(
            state = IncomingState(
                isLoading = false,
                task = Task(
                    "t1", "p1", "Sponsor deck — final pass",
                    "Needs the sponsor's final sign-off before it goes to print.",
                    "u2", "Daniel Osei", TaskStatus.DOING, System.currentTimeMillis() + 86_400_000L, 3, "u1", "You", 0, 0
                ),
                handoff = Handoff("h1", "t1", "u2", "Daniel Osei", "u1", "You", "Design's done — needs a print quote before Friday.", HandoffStatus.OFFERED, null, 0, null)
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomingScreenLoadingPreview() {
    TemackerTheme {
        IncomingScreen(state = IncomingState(isLoading = true), onAction = {}, onNavigateBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomingScreenUnavailablePreview() {
    TemackerTheme {
        IncomingScreen(state = IncomingState(isLoading = false, isTrailLoaded = true), onAction = {}, onNavigateBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun IncomingScreenAlreadyAcceptedPreview() {
    TemackerTheme {
        IncomingScreen(
            state = IncomingState(
                isLoading = false,
                isTrailLoaded = true,
                task = Task("t1", "p1", "Sponsor deck — final pass", null, "u1", "You", TaskStatus.DOING, null, 3, "u1", "You", 0, 0),
                handoff = Handoff("h1", "t1", "u2", "Daniel Osei", "u1", "You", null, HandoffStatus.ACCEPTED, null, 0, 0)
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
