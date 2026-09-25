package com.example.temacker.feature_auth.presentation.login

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temacker.R
import com.example.temacker.core.presentation.designsystem.AppColors
import com.example.temacker.core.presentation.designsystem.Ink500
import com.example.temacker.core.presentation.designsystem.Ink900
import com.example.temacker.core.presentation.designsystem.Line
import com.example.temacker.core.presentation.designsystem.Spacing
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.designsystem.White
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
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
    val reducedMotion = rememberReducedMotion()
    var brandVisible by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(reducedMotion) { if (!reducedMotion) brandVisible = true }
    val brandAlpha by animateFloatAsState(if (brandVisible) 1f else 0f, tween(360), label = "loginBrandAlpha")
    val subtitleAlpha by animateFloatAsState(if (brandVisible) 1f else 0f, tween(360, delayMillis = 90), label = "loginSubtitleAlpha")

    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 26.dp)
        ) {

            // Brand block — mirrors the spec's .login-brand. No weight(): the Column is now scrollable,
            // which gives it unbounded height and would crash ("infinite max height") like Spacer(weight()) did.
            Column(
                modifier = Modifier.padding(top = 80.dp, bottom = Spacing.xl),
                horizontalAlignment = Alignment.Start
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_temacker_mark),
                    contentDescription = null,
                    modifier = Modifier.height(58.dp).alpha(brandAlpha)
                )
                Text(
                    text = "Temacker",
                    color = Ink900,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.035f).em,
                    modifier = Modifier.padding(top = 22.dp).alpha(brandAlpha)
                )
                Text(
                    text = "Keep every project, person and role moving in one place.",
                    color = Ink500,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    modifier = Modifier.padding(top = 14.dp).alpha(subtitleAlpha)
                )
            }

            state.error?.let { error ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.m)
                        .background(AppColors.dangerWash, RoundedCornerShape(12.dp))
                        .padding(horizontal = Spacing.m, vertical = Spacing.s)
                ) {
                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = AppColors.danger)
                    Text(
                        text = error.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.onDanger,
                        modifier = Modifier.padding(start = Spacing.s)
                    )
                }
            }

            // Foot — Google first per spec (the primary route), email/password second.
            Column(modifier = Modifier.padding(bottom = 26.dp)) {
                OutlinedButton(
                    onClick = { onAction(LoginAction.OnGoogleSignInClick) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = White,
                        contentColor = Ink900
                    ),
                    border = BorderStroke(1.5.dp, Line),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.height(21.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Continue with Google")
                }
                Text(
                    text = "ONE-TAP SECURE SIGN-IN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 13.dp, bottom = 13.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Line)
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink500,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Line)
                }
                Spacer(modifier = Modifier.height(13.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onAction(LoginAction.OnEmailChange(it)) },
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onAction(LoginAction.OnPasswordChange(it)) },
                    label = { Text("Password") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onAction(LoginAction.OnEmailSubmit)
                    }),
                    trailingIcon = {
                        TextButton(onClick = { onAction(LoginAction.OnTogglePasswordVisibility) }) {
                            Text(if (state.isPasswordVisible) "Hide" else "Show")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onAction(LoginAction.OnEmailSubmit) },
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(if (state.isRegisterMode) "Create account" else "Sign in")
                }

                TextButton(
                    onClick = { onAction(LoginAction.OnToggleMode) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (state.isRegisterMode) "Already have an account? Sign in" else "Don't have an account? Sign up"
                    )
                }

                Text(
                    text = "By continuing you agree to the Terms and Privacy Policy.",
                    fontSize = 11.5.sp,
                    lineHeight = 18.sp,
                    color = Ink500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenDefaultPreview() {
    TemackerTheme {
        LoginScreen(state = LoginState(), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenRegisterModePreview() {
    TemackerTheme {
        LoginScreen(state = LoginState(isRegisterMode = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    TemackerTheme {
        LoginScreen(state = LoginState(isLoading = true), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    TemackerTheme {
        LoginScreen(
            state = LoginState(error = UiText.DynamicString("No internet connection.")),
            onAction = {}
        )
    }
}
