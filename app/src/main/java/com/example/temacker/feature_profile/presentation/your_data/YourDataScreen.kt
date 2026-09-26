package com.example.temacker.feature_profile.presentation.your_data

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.components.TmkButtonVariant
import com.example.temacker.core.presentation.designsystem.AppColors
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import com.example.temacker.feature_profile.domain.model.ExportFormat
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun YourDataRoot(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: YourDataViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            YourDataEvent.NavigateBack -> onNavigateBack()
            YourDataEvent.AccountDeleted -> onAccountDeleted()
            is YourDataEvent.ShareExportFile -> {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(event.filePath))
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (event.filePath.endsWith(".json")) "application/json" else "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export project data"))
            }
        }
    }

    YourDataScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourDataScreen(state: YourDataState, onAction: (YourDataAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Your data") },
                navigationIcon = {
                    IconButton(onClick = { onAction(YourDataAction.OnBackClick) }) {
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
                    Text(
                        text = error.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onAction(YourDataAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }

            if (state.isLoading) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column {
                        Text(
                            "Everything, including the parts you'd need in order to leave",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Ink900
                        )
                        Text(
                            "One file per project, from ${state.projectName}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink500,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            CountRow(label = "Tasks", value = state.taskCount)
                            CountRow(label = "Handoffs, with notes and reasons", value = state.handoffCount)
                            CountRow(label = "Members and their roles", value = state.memberCount)
                        }
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        ExportFormat.entries.forEachIndexed { index, format ->
                            SegmentedButton(
                                selected = state.exportFormat == format,
                                onClick = { onAction(YourDataAction.OnFormatSelected(format)) },
                                shape = SegmentedButtonDefaults.itemShape(index, ExportFormat.entries.size)
                            ) {
                                Text(format.name)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = Ink500, modifier = Modifier.size(20.dp))
                        Text(
                            "If Temacker ever stops being maintained, nobody loses their history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink500,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }

                    // Danger zone — kept well below the primary Export action, not stacked right above it.
                    Card(colors = CardDefaults.cardColors(containerColor = AppColors.dangerWash), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.m)) {
                            Text("DANGER ZONE", style = MaterialTheme.typography.labelSmall, color = AppColors.onDanger)
                            Text(
                                "Permanent. You'll leave every project you're a member of.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.onDanger,
                                modifier = Modifier.padding(top = 4.dp, bottom = Spacing.s)
                            )
                            TmkButton(
                                text = if (state.isDeletingAccount) "Deleting…" else "Delete my account and data",
                                onClick = { onAction(YourDataAction.OnDeleteAccountClick) },
                                variant = TmkButtonVariant.DESTRUCTIVE,
                                isLoading = state.isDeletingAccount
                            )
                        }
                    }
                }

                TmkButton(
                    text = if (state.isExporting) "Exporting…" else "Export ${state.projectName}",
                    onClick = { onAction(YourDataAction.OnExportClick) },
                    isLoading = state.isExporting,
                    icon = Icons.Rounded.Download,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onAction(YourDataAction.OnDismissDeleteConfirm) },
            title = { Text("Delete your account?") },
            text = { Text("You'll leave every project you're a member of and your Firebase account will be gone. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { onAction(YourDataAction.OnConfirmDeleteClick) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { onAction(YourDataAction.OnDismissDeleteConfirm) }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CountRow(label: String, value: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink500)
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Ink900)
    }
}

@Preview(showBackground = true)
@Composable
private fun YourDataScreenPreview() {
    TemackerTheme {
        YourDataScreen(
            state = YourDataState(projectName = "Aurora Launch", taskCount = 62, handoffCount = 214, memberCount = 8, isLoading = false),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YourDataScreenCsvSelectedPreview() {
    TemackerTheme {
        YourDataScreen(
            state = YourDataState(
                projectName = "Aurora Launch", taskCount = 62, handoffCount = 214, memberCount = 8,
                exportFormat = ExportFormat.CSV, isLoading = false
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YourDataScreenLoadingPreview() {
    TemackerTheme { YourDataScreen(state = YourDataState(isLoading = true), onAction = {}) }
}

@Preview(showBackground = true)
@Composable
private fun YourDataScreenDeleteConfirmPreview() {
    TemackerTheme {
        YourDataScreen(
            state = YourDataState(projectName = "Aurora Launch", taskCount = 62, handoffCount = 214, memberCount = 8, isLoading = false, showDeleteConfirm = true),
            onAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YourDataScreenErrorPreview() {
    TemackerTheme {
        YourDataScreen(
            state = YourDataState(
                projectName = "Aurora Launch", isLoading = false,
                error = UiText.DynamicString("You're the Leader of Aurora Launch. Run Succession there first.")
            ),
            onAction = {}
        )
    }
}
