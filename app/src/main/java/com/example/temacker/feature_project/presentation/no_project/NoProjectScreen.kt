package com.example.temacker.feature_project.presentation.no_project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.temacker.R
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun NoProjectRoot(
    onNavigateToCreateProject: () -> Unit,
    onNavigateToJoinProject: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: NoProjectViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            NoProjectEvent.NavigateToCreateProject -> onNavigateToCreateProject()
            NoProjectEvent.NavigateToJoinProject -> onNavigateToJoinProject()
            NoProjectEvent.NavigateToProfile -> onNavigateToProfile()
        }
    }

    NoProjectScreen(onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoProjectScreen(onAction: (NoProjectAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_temacker_mark),
                            contentDescription = null,
                            modifier = Modifier.height(26.dp)
                        )
                        Text(
                            text = "Temacker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = (-0.02f).em,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(NoProjectAction.OnProfileClick) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Ink500,
                    modifier = Modifier.height(48.dp)
                )
                Text(
                    text = "FIRST STEP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                    modifier = Modifier.padding(top = 22.dp)
                )
                Text(
                    text = "Start or join a project",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink900,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = "Projects are where your team's people, roles and tasks live. Create one, or join an existing project with a code from its Leader.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { onAction(NoProjectAction.OnCreateProjectClick) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.height(20.dp))
                    Text("Create a project", modifier = Modifier.padding(start = 8.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onAction(NoProjectAction.OnJoinProjectClick) },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("I have an invite code")
                }

                Text(
                    text = "You can belong to more than one project later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoProjectScreenPreview() {
    TemackerTheme {
        NoProjectScreen(onAction = {})
    }
}
