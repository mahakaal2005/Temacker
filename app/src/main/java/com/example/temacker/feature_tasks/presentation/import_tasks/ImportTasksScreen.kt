package com.example.temacker.feature_tasks.presentation.import_tasks

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.FileUpload
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.core.presentation.components.EmptyState
import com.example.temacker.core.presentation.components.TmkButton
import com.example.temacker.core.presentation.components.TmkButtonVariant
import com.example.temacker.core.presentation.designsystem.AmberInk
import com.example.temacker.core.presentation.designsystem.AmberWash
import com.example.temacker.core.presentation.designsystem.CoralInk
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.White
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.feature_tasks.domain.import_tasks.ImportPreview
import com.example.temacker.feature_tasks.domain.import_tasks.ImportedTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

private const val MAX_FILE_BYTES = 1_000_000

@Composable
fun ImportTasksRoot(
    onNavigateBack: () -> Unit,
    onNavigateToBoard: () -> Unit,
    viewModel: ImportTasksViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            ImportTasksEvent.NavigateBack -> onNavigateBack()
            ImportTasksEvent.NavigateToBoard -> onNavigateToBoard()
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val bytes = input.readNBytesCapped(MAX_FILE_BYTES + 1)
                        if (bytes.size > MAX_FILE_BYTES) null else bytes.decodeToString()
                    }
                }.getOrNull()
            }
            viewModel.onAction(if (text == null) ImportTasksAction.OnFileUnreadable else ImportTasksAction.OnFileRead(text))
        }
    }

    ImportTasksScreen(
        state = state,
        onAction = viewModel::onAction,
        onPickFile = { picker.launch(arrayOf("application/json", "text/plain", "*/*")) }
    )
}

private fun java.io.InputStream.readNBytesCapped(limit: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    while (out.size() < limit) {
        val read = read(buffer, 0, minOf(buffer.size, limit - out.size()))
        if (read < 0) break
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTasksScreen(state: ImportTasksState, onAction: (ImportTasksAction) -> Unit, onPickFile: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Import tasks") },
                navigationIcon = {
                    IconButton(onClick = { onAction(ImportTasksAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
            state.error?.let { error ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.asString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onAction(ImportTasksAction.OnErrorDismissed) }) { Text("Dismiss") }
                }
            }
            val preview = state.preview
            if (preview == null) {
                EmptyState(
                    icon = Icons.Rounded.FileUpload,
                    title = "Import tasks from a file",
                    body = "Choose a JSON file. Each task can name a teammate in \"assignee\", and it lands on their Board.",
                    primaryLabel = "Choose file",
                    onPrimaryClick = onPickFile,
                    modifier = Modifier.weight(1f)
                )
                FormatHint()
            } else {
                PreviewList(preview, modifier = Modifier.weight(1f))
                Column(modifier = Modifier.padding(Spacing.l)) {
                    TmkButton(
                        text = "Create ${preview.tasks.size} ${if (preview.tasks.size == 1) "task" else "tasks"}",
                        onClick = { onAction(ImportTasksAction.OnImportClick) },
                        enabled = preview.tasks.isNotEmpty(),
                        isLoading = state.isImporting,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TmkButton(
                        text = "Choose another file",
                        onClick = { onAction(ImportTasksAction.OnChooseAnotherClick) },
                        variant = TmkButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatHint() {
    Surface(color = AmberWash, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.m)) {
        Text(
            "{\"tasks\": [{\"title\": \"Book venue\", \"description\": \"optional\", \"dueDate\": \"2026-10-31\", \"assignee\": \"Tomas Vidal\"}]}",
            style = MaterialTheme.typography.bodySmall,
            color = AmberInk,
            modifier = Modifier.padding(Spacing.s)
        )
    }
}

@Composable
private fun PreviewList(preview: ImportPreview, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.l), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.xs)) {
        items(preview.problems) { problem ->
            Text(problem, style = MaterialTheme.typography.bodySmall, color = CoralInk)
        }
        items(preview.tasks) { task -> PreviewRow(task) }
    }
}

private val dueFormat get() = SimpleDateFormat("EEE d MMM", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }

@Composable
private fun PreviewRow(task: ImportedTask) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(16.dp))
            .androidBorder()
            .padding(Spacing.s)
    ) {
        Text(task.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        Text(
            if (task.matched) "For ${task.assignee.displayName}"
            else if (task.assigneeQuery != null) "No member named \"${task.assigneeQuery}\", so it will be yours"
            else "For you",
            style = MaterialTheme.typography.bodySmall,
            color = if (task.assigneeQuery != null && !task.matched) CoralInk else Ink500
        )
        task.dueDate?.let { Text("Due ${dueFormat.format(Date(it))}", style = MaterialTheme.typography.bodySmall, color = Ink500) }
    }
}

private fun Modifier.androidBorder(): Modifier = this.then(Modifier.border(1.dp, Line, RoundedCornerShape(16.dp)))

@Preview(showBackground = true)
@Composable
private fun ImportTasksEmptyPreview() {
    TemackerTheme { ImportTasksScreen(state = ImportTasksState(), onAction = {}, onPickFile = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ImportTasksPreviewListPreview() {
    val me = ProjectMember("me", "Lena Leader", null, "Leader", true, true)
    val tomas = ProjectMember("u2", "Tomas Vidal", null, "Default", false, false)
    TemackerTheme {
        ImportTasksScreen(
            state = ImportTasksState(
                preview = ImportPreview(
                    listOf(
                        ImportedTask("Book venue", null, 1793404800000L, "Tomas Vidal", tomas, true),
                        ImportedTask("Order snacks", "Vegan too", null, "Nobody", me, false)
                    ),
                    listOf("Task 3 has no title.")
                )
            ),
            onAction = {},
            onPickFile = {}
        )
    }
}
