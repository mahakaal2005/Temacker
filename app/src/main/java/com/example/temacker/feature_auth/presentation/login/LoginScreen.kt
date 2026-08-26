package com.example.temacker.feature_auth.presentation.login

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.core.presentation.util.ObserveAsEvents
import com.example.temacker.core.presentation.util.UiText
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginRoot(
    onNavigateToApp: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LoginEvent.NavigateToApp -> onNavigateToApp()
        }
    }

    LoginScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun LoginScreen(state: LoginState, onAction: (LoginAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Temacker", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.padding(top = 32.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { onAction(LoginAction.OnGoogleSignInClick) }) {
                    Text("Sign in with Google")
                }
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.padding(top = 16.dp))
                Text(text = error.asString(), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenDefaultPreview() {
    MaterialTheme {
        LoginScreen(state = LoginState(), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenLoadingPreview() {
    MaterialTheme {
        LoginScreen(state = LoginState(isLoading = true), onAction = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginScreenErrorPreview() {
    MaterialTheme {
        LoginScreen(
            state = LoginState(error = UiText.DynamicString("No internet connection.")),
            onAction = {}
        )
    }
}
