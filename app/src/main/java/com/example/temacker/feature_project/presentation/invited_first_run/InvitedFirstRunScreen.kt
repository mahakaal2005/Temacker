package com.example.temacker.feature_project.presentation.invited_first_run

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink700
import com.example.temacker.core.presentation.designsystem.NeutralWash
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.presentation.role_copy.ALWAYS_ALLOWED
import com.example.temacker.feature_project.presentation.role_copy.CapabilityRow
import org.koin.androidx.compose.koinViewModel

@Composable
fun InvitedFirstRunRoot(
    onNavigateToBoard: () -> Unit,
    viewModel: InvitedFirstRunViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            InvitedFirstRunEvent.NavigateToBoard -> onNavigateToBoard()
        }
    }

    // Back skips to the board too, so it never lands on the stale no-project screen.
    BackHandler { viewModel.onAction(InvitedFirstRunAction.OnGoToBoardClick) }

    InvitedFirstRunScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun InvitedFirstRunScreen(state: InvitedFirstRunState, onAction: (InvitedFirstRunAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Header(state)
                    Section(title = "WHAT YOU CAN DO NOW", items = state.canDo, allowed = true)
                    if (state.needsRole.isNotEmpty()) {
                        Section(title = "WHAT NEEDS A ROLE", items = state.needsRole, allowed = false)
                        state.leaderName?.let {
                            Text("Ask $it, the Leader, for a role.", style = MaterialTheme.typography.bodyMedium, color = Ink500)
                        }
                    }
                }
                state.error?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            }
            Button(
                onClick = { onAction(InvitedFirstRunAction.OnGoToBoardClick) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp).height(56.dp)
            ) { Text("Go to the board") }
        }
    }
}

@Composable
private fun Header(state: InvitedFirstRunState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "You're in — ${state.projectName}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        state.addedByLine?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink500) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = state.roleName,
                style = MaterialTheme.typography.labelLarge,
                color = Ink700,
                modifier = Modifier.background(NeutralWash, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp)
            )
            if (state.hasNoPermissions) Text("No permissions yet", style = MaterialTheme.typography.bodyMedium, color = Ink500)
        }
    }
}

@Composable
private fun Section(title: String, items: List<String>, allowed: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = Ink500, modifier = Modifier.semantics { heading() })
        items.forEachIndexed { index, item ->
            if (allowed) {
                StaggerIn(index = index) { CapabilityRow(label = item, allowed = true) }
            } else {
                CapabilityRow(label = item, allowed = false)
            }
        }
    }
}

// Only the "what you can do now" rows stagger in — the celebratory half of the screen, per spec.
// The locked rows stay static so the eye isn't drawn to what's off-limits.
@Composable
private fun StaggerIn(index: Int, content: @Composable () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    var visible by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(reducedMotion) { if (!reducedMotion) visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(220, delayMillis = index * 45),
        label = "capabilityRowStagger"
    )
    Column(modifier = Modifier.alpha(alpha)) { content() }
}

private val previewNeeds = listOf("Invite members", "Change roles", "Remove members", "Create tasks", "Delete tasks — permanent, cannot be undone")

@Preview(showBackground = true)
@Composable
private fun InvitedFirstRunDefaultPreview() {
    TemackerTheme {
        InvitedFirstRunScreen(
            state = InvitedFirstRunState(
                projectName = "Aurora Launch", roleName = "Default", addedByLine = "Priya Raman added you a moment ago.",
                canDo = ALWAYS_ALLOWED, needsRole = previewNeeds, leaderName = "Priya Raman", isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InvitedFirstRunNoInviterPreview() {
    TemackerTheme {
        InvitedFirstRunScreen(
            state = InvitedFirstRunState(
                projectName = "Aurora Launch", roleName = "Default",
                canDo = ALWAYS_ALLOWED, needsRole = previewNeeds, isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InvitedFirstRunPartialRolePreview() {
    TemackerTheme {
        InvitedFirstRunScreen(
            state = InvitedFirstRunState(
                projectName = "Aurora Launch", roleName = "Editor", addedByLine = "Priya Raman added you a moment ago.",
                hasNoPermissions = false, canDo = ALWAYS_ALLOWED + "Create tasks",
                needsRole = previewNeeds - "Create tasks", leaderName = "Priya Raman", isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InvitedFirstRunLoadingPreview() {
    TemackerTheme { InvitedFirstRunScreen(state = InvitedFirstRunState(), onAction = {}) }
}

@Preview(showBackground = true)
@Composable
private fun InvitedFirstRunErrorPreview() {
    TemackerTheme {
        InvitedFirstRunScreen(
            state = InvitedFirstRunState(error = UiText.DynamicString("You don't have permission to do that.")),
            onAction = {}
        )
    }
}
