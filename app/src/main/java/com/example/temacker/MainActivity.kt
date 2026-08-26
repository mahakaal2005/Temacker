package com.example.temacker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.temacker.feature_auth.domain.repository.AuthRepository
import com.example.temacker.feature_auth.presentation.navigation.SplashRoute
import com.example.temacker.feature_auth.presentation.navigation.authGraph
import com.example.temacker.ui.theme.TemackerTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemackerTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = SplashRoute) {
                    authGraph(
                        navController = navController,
                        onNavigateToApp = {
                            navController.navigate(AppPlaceholderRoute) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                    composable<AppPlaceholderRoute> { AppPlaceholderScreen() }
                }
            }
        }
    }
}

// Stand-in for feature_project's real Home screen — deleted once that feature exists.
@Serializable private data object AppPlaceholderRoute

@Composable
private fun AppPlaceholderScreen(authRepository: AuthRepository = koinInject()) {
    val user by authRepository.observeUser().collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Signed in as ${user?.displayName ?: "…"}", style = MaterialTheme.typography.titleMedium)
            Text("feature_project builds the real Home screen next.")
            Button(onClick = { scope.launch { authRepository.signOut() } }) {
                Text("Sign out")
            }
        }
    }
}
