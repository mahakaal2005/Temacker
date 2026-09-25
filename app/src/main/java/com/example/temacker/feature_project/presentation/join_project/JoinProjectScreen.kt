package com.example.temacker.feature_project.presentation.join_project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun JoinProjectRoot(
    onNavigateBack: () -> Unit,
    onNavigateToFirstRun: () -> Unit,
    viewModel: JoinProjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            JoinProjectEvent.NavigateBack -> onNavigateBack()
            JoinProjectEvent.NavigateToFirstRun -> onNavigateToFirstRun()
        }
    }

    JoinProjectScreen(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinProjectScreen(state: JoinProjectState, onAction: (JoinProjectAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Join project") },
                navigationIcon = {
                    IconButton(onClick = { onAction(JoinProjectAction.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            val clipboardManager = LocalClipboardManager.current

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "INVITE CODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
                OutlinedTextField(
                    value = state.code,
                    onValueChange = { onAction(JoinProjectAction.OnCodeChange(it)) },
                    label = { Text("Invite code") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    visualTransformation = JoinCodeVisualTransformation,
                    isError = state.error != null,
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.getText()?.text?.let { onAction(JoinProjectAction.OnCodeChange(it)) }
                        }) {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste")
                        }
                    },
                    supportingText = state.error?.let { error -> { Text(error.asString(), color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = Ink500, modifier = Modifier.height(20.dp))
                    Text(
                        text = "Codes look like TMK-XXXX-XX and stop working 7 days after a Leader creates them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink500,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Button(
                    onClick = { onAction(JoinProjectAction.OnJoinClick) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Join project")
                    }
                }
            }
        }
    }
}

// Display-only: reformats whatever's in the field as TMK-XXXX-XX (3-4-2). state.code (the real
// submitted value) is never touched — GenerateInviteCodeUseCase generates codes already dashed
// ("TMK-${4 chars}-${2 chars}"), so a real invite code pasted in here already carries dashes; this
// strips any punctuation first and reinserts them by position, so it renders correctly either way.
// Note: if a user hand-types the 9 letters with no dashes at all, state.code stays undashed and the
// join call likely won't match the backend's dashed format — that's a pre-existing ViewModel gap,
// out of scope to fix here per the phase's presentation-only boundary (flagging, not changing it).
private val JoinCodeVisualTransformation = VisualTransformation { text ->
    val raw = text.text.filter { it.isLetterOrDigit() }.take(9)
    val formatted = buildString {
        raw.forEachIndexed { index, char ->
            if (index == 3 || index == 7) append('-')
            append(char)
        }
    }
    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val alnumOffset = text.text.take(offset).count { it.isLetterOrDigit() }
            return when {
                alnumOffset <= 3 -> alnumOffset
                alnumOffset <= 7 -> alnumOffset + 1
                else -> alnumOffset + 2
            }.coerceAtMost(formatted.length)
        }

        override fun transformedToOriginal(offset: Int): Int = when {
            offset <= 3 -> offset
            offset <= 8 -> offset - 1
            else -> offset - 2
        }.coerceIn(0, text.text.length)
    }
    TransformedText(AnnotatedString(formatted), offsetMapping)
}

@Preview(showBackground = true)
@Composable
private fun JoinProjectScreenDefaultPreview() {
    TemackerTheme {
        JoinProjectScreen(state = JoinProjectState(code = "TMK-4K8Z-QD"), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun JoinProjectScreenErrorPreview() {
    TemackerTheme {
        JoinProjectScreen(
            state = JoinProjectState(
                code = "TMK-4K8Z-QD",
                error = UiText.DynamicString("This code expired. Ask your project Leader for a new one.")
            ),
            onAction = {}
        )
    }
}
