package com.example.temacker.feature_profile.presentation.plan_limits

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.effectsSpring
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlanLimitsRoot(
    onNavigateBack: () -> Unit,
    viewModel: PlanLimitsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PlanLimitsEvent.NavigateBack -> onNavigateBack()
        }
    }

    PlanLimitsScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanLimitsScreen(state: PlanLimitsState, onAction: (PlanLimitsAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Plan & limits") },
                navigationIcon = {
                    IconButton(onClick = { onAction(PlanLimitsAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            state.error?.let { error ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(PlanLimitsAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            if (state.isLoading) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(colors = CardDefaults.cardColors(containerColor = AmberWash), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Free — for a single team", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberInk)
                            Text(
                                "Permanently. This is not a trial, and there is no card on file.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            UsageMeter(
                                label = "Tasks in ${state.projectName}",
                                count = state.taskCount,
                                cap = TASK_CAP,
                                color = MaterialTheme.colorScheme.primary
                            )
                            UsageMeter(label = "Members", count = state.memberCount, cap = MEMBER_CAP, color = TealInk)
                        }
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = AmberInk, modifier = Modifier.size(20.dp))
                        Text(
                            "Temacker is free for teams like yours, permanently. Limits exist to keep it affordable to run, not to sell you an upgrade.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberInk,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = NeutralWash), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("For organisations", style = MaterialTheme.typography.titleMedium, color = Ink900, modifier = Modifier.weight(1f))
                                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(50)) {
                                    Text(
                                        "Not built yet",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Ink500,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                "If money ever appears it comes from here — rollups across many projects, the exported audit trail, retention beyond a year, SSO. Never from a volunteer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageMeter(label: String, count: Int, cap: Int, color: androidx.compose.ui.graphics.Color) {
    val target = (count.toFloat() / cap).coerceIn(0f, 1f)
    var animateFrom0 by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateFrom0 = true }
    val progress by animateFloatAsState(
        targetValue = if (animateFrom0) target else 0f,
        animationSpec = effectsSpring(),
        label = "usageMeterProgress"
    )
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink500)
            Row {
                Text("$count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Ink900)
                Text(" of $cap", style = MaterialTheme.typography.bodyMedium, color = Ink500)
            }
        }
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = NeutralWash,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanLimitsScreenPreview() {
    TemackerTheme {
        PlanLimitsScreen(state = PlanLimitsState(projectName = "Aurora Launch", taskCount = 62, memberCount = 8, isLoading = false), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanLimitsScreenLoadingPreview() {
    TemackerTheme { PlanLimitsScreen(state = PlanLimitsState(isLoading = true), onAction = {}) }
}

@Preview(showBackground = true)
@Composable
private fun PlanLimitsScreenNearCapPreview() {
    TemackerTheme {
        PlanLimitsScreen(state = PlanLimitsState(projectName = "Aurora Launch", taskCount = 190, memberCount = 24, isLoading = false), onAction = {})
    }
}
