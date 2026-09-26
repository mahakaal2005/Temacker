package com.example.temacker.feature_project.presentation.switch_project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.components.TmkButtonVariant
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.TealWash
import com.example.temacker.core.presentation.designsystem.White
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.memberCountLabel
import com.example.temacker.core.presentation.designsystem.TealInk
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun SwitchProjectRoot(
    onNavigateBack: () -> Unit,
    onNavigateToJoinProject: () -> Unit,
    onNavigateToCreateProject: () -> Unit,
    viewModel: SwitchProjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SwitchProjectEvent.NavigateBack -> onNavigateBack()
        }
    }

    SwitchProjectScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onNavigateToJoinProject = onNavigateToJoinProject,
        onNavigateToCreateProject = onNavigateToCreateProject
    )
}

@Composable
fun SwitchProjectScreen(
    state: SwitchProjectState,
    onAction: (SwitchProjectAction) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToJoinProject: () -> Unit,
    onNavigateToCreateProject: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Switch project", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingBody(padding)
            state.error != null -> ErrorBody(padding, state.error.asString())
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "header") {
                    Text("Your projects", style = MaterialTheme.typography.labelLarge, color = Ink500, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
                }
                items(state.rows, key = { it.projectId }) { row ->
                    ProjectRow(row, onClick = { onAction(SwitchProjectAction.OnProjectClick(row.projectId)) })
                }
                item(key = "actions") {
                    Column(modifier = Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TmkButton(text = "Create a new project", onClick = onNavigateToCreateProject, icon = Icons.Rounded.Add)
                        TmkButton(
                            text = "Join another project",
                            onClick = onNavigateToJoinProject,
                            variant = TmkButtonVariant.SECONDARY,
                            icon = Icons.Rounded.Add
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingBody(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(top = 48.dp))
    }
}

@Composable
private fun ErrorBody(padding: PaddingValues, message: String) {
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ProjectRow(row: SwitchProjectRow, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (row.isSelected) AmberWash else White,
        border = BorderStroke(if (row.isSelected) 1.5.dp else 1.dp, if (row.isSelected) Amber else Line),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(if (row.isSelected) Amber.copy(alpha = 0.35f) else AmberWash, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(row.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberInk)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(row.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${memberCountLabel(row.memberCount)} · ${roleLabel(row.roleName)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink500
                )
            }
            if (row.isSelected) {
                Surface(shape = RoundedCornerShape(50), color = TealWash) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = TealInk, modifier = Modifier.size(14.dp))
                        Text("Current", style = MaterialTheme.typography.labelMedium, color = TealInk, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            } else {
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Ink500)
            }
        }
    }
}

// Members with no named role are stored as "Default", which reads like a setting rather than a role.
private fun roleLabel(roleName: String) = if (roleName == "Default") "Member" else roleName

@Preview(showBackground = true)
@Composable
private fun SwitchProjectScreenPreview() {
    TemackerTheme {
        SwitchProjectScreen(
            state = SwitchProjectState(
                isLoading = false,
                rows = listOf(
                    SwitchProjectRow("p1", "Aurora Launch", 8, "Leader", isSelected = true),
                    SwitchProjectRow("p2", "Cycle 2", 5, "Default", isSelected = false)
                )
            ),
            onAction = {},
            onNavigateBack = {},
            onNavigateToJoinProject = {},
            onNavigateToCreateProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchProjectScreenLoadingPreview() {
    TemackerTheme {
        SwitchProjectScreen(
            state = SwitchProjectState(isLoading = true),
            onAction = {},
            onNavigateBack = {},
            onNavigateToJoinProject = {},
            onNavigateToCreateProject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchProjectScreenErrorPreview() {
    TemackerTheme {
        SwitchProjectScreen(
            state = SwitchProjectState(
                isLoading = false,
                error = com.example.temacker.core.presentation.util.UiText.DynamicString("Couldn't load your projects.")
            ),
            onAction = {},
            onNavigateBack = {},
            onNavigateToJoinProject = {},
            onNavigateToCreateProject = {}
        )
    }
}
