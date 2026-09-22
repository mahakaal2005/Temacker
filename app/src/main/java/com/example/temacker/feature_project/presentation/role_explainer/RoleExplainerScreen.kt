package com.example.temacker.feature_project.presentation.role_explainer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink700
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_project.presentation.role_copy.Capability
import com.example.temacker.feature_project.presentation.role_copy.CapabilityGroup
import com.example.temacker.feature_project.presentation.role_copy.CapabilityRow
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RoleExplainerRoot(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: RoleExplainerViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            RoleExplainerEvent.NavigateBack -> onNavigateBack()
        }
    }

    RoleExplainerScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleExplainerScreen(state: RoleExplainerState, onAction: (RoleExplainerAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(if (state.roleName.isBlank()) "What a role means" else "What ${state.roleName} can do") },
                navigationIcon = {
                    IconButton(onClick = { onAction(RoleExplainerAction.OnBackClick) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when {
                    state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    state.error != null -> Text(state.error.asString(), color = MaterialTheme.colorScheme.error)
                    else -> {
                        Text("${state.memberName} · ${state.roleName}", style = MaterialTheme.typography.bodyLarge, color = Ink700)
                        if (state.showReadOnlyNote) {
                            Text("Read-only. Only the Leader can change a role.", style = MaterialTheme.typography.bodyMedium, color = Ink500)
                        }
                        CapabilityGroup.entries.forEach { group ->
                            Group(title = if (group == CapabilityGroup.TEAM) "TEAM" else "TASKS", group = group, granted = state.granted)
                        }
                        state.setLine?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink500) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Group(title: String, group: CapabilityGroup, granted: List<Capability>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = Ink500, modifier = Modifier.semantics { heading() })
        Capability.entries.filter { it.group == group }.forEach { capability ->
            CapabilityRow(label = capability.label, allowed = capability in granted, lockedColor = Ink500)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoleExplainerDefaultPreview() {
    TemackerTheme {
        RoleExplainerScreen(
            state = RoleExplainerState(memberName = "Mei-Ling Chow", roleName = "Default", setLine = "Set by Priya Raman · 14 Aug", isLoading = false),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoleExplainerLeaderPreview() {
    TemackerTheme {
        RoleExplainerScreen(
            state = RoleExplainerState(
                memberName = "Priya Raman", roleName = "Leader", granted = Capability.entries.toList(),
                setLine = "Set by Priya Raman · 14 Aug", showReadOnlyNote = false, isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoleExplainerPartialPreview() {
    TemackerTheme {
        RoleExplainerScreen(
            state = RoleExplainerState(
                memberName = "Daniel Osei", roleName = "Reviewer",
                granted = listOf(Capability.INVITE_MEMBERS, Capability.CREATE_TASKS), isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoleExplainerLoadingPreview() {
    TemackerTheme { RoleExplainerScreen(state = RoleExplainerState(), onAction = {}) }
}

@Preview(showBackground = true)
@Composable
private fun RoleExplainerErrorPreview() {
    TemackerTheme {
        RoleExplainerScreen(
            state = RoleExplainerState(isLoading = false, error = UiText.DynamicString("This member is no longer in the project.")),
            onAction = {}
        )
    }
}
