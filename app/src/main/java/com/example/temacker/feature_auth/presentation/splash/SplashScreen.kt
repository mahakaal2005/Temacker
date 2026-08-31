package com.example.temacker.feature_auth.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.temacker.R
import com.example.temacker.core.presentation.designsystem.Amber
import com.example.temacker.core.presentation.designsystem.DPrimary
import com.example.temacker.core.presentation.designsystem.DSecondary
import com.example.temacker.core.presentation.designsystem.Navy900
import com.example.temacker.core.presentation.designsystem.TemackerTheme
import com.example.temacker.core.presentation.util.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashRoot(
    onNavigateToLogin: () -> Unit,
    onNavigateToApp: () -> Unit,
    viewModel: SplashViewModel = koinViewModel()
) {
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SplashEvent.NavigateToLogin -> onNavigateToLogin()
            SplashEvent.NavigateToApp -> onNavigateToApp()
        }
    }

    SplashScreen()
}

@Composable
fun SplashScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = Navy900) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Amber.copy(alpha = 0.17f), Amber.copy(alpha = 0.05f), Color.Transparent)
                                )
                            )
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_temacker_mark),
                        contentDescription = null,
                        modifier = Modifier.size(88.dp)
                    )
                }

                Text(
                    text = "Temacker",
                    color = DPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.035f).em,
                    modifier = Modifier.padding(top = 26.dp)
                )
                Text(
                    text = "TEAM COORDINATION",
                    color = Amber,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 52.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Amber,
                    trackColor = DPrimary.copy(alpha = 0.16f),
                    strokeWidth = 2.6.dp,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Checking your session…",
                    color = DSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 15.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    TemackerTheme {
        SplashScreen()
    }
}
