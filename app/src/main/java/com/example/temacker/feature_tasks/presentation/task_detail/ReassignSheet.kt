package com.example.temacker.feature_tasks.presentation.task_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.temacker.core.domain.model.ProjectMember
import com.example.temacker.core.presentation.components.TmkSheet
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReassignSheet(members: List<ProjectMember>, onSelect: (String) -> Unit, onDismiss: () -> Unit, title: String = "Reassign to") {
    TmkSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.l)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (members.isEmpty()) Text("No one else is in this project yet.", style = MaterialTheme.typography.bodyMedium, color = Ink500, modifier = Modifier.padding(top = Spacing.s))
            members.forEach { member ->
                Column(modifier = Modifier.fillMaxWidth().clickable { onSelect(member.uid) }.padding(vertical = 12.dp)) {
                    Text(member.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(member.roleName, style = MaterialTheme.typography.bodySmall, color = Ink500)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReassignSheetPreview() {
    TemackerTheme {
        ReassignSheet(
            members = listOf(ProjectMember("u2", "Tomas Vidal", null, "Scheduler", false, false), ProjectMember("u3", "Aisha Bello", null, "Default", false, false)),
            onSelect = {},
            onDismiss = {}
        )
    }
}
