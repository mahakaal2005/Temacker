package com.example.temacker.feature_auth.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.fadeIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.example.temacker.core.presentation.designsystem.rememberReducedMotion
import com.example.temacker.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.delay
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
    val reducedMotion = rememberReducedMotion()
    // The mark starts fully visible so it continues the system launch window seamlessly; the words rise in after it.
    var revealed by remember { mutableStateOf(reducedMotion) }
    LaunchedEffect(Unit) { revealed = true }
    val titleAlpha by animateFloatAsState(if (revealed) 1f else 0f, tween(700, delayMillis = 80, easing = FastOutSlowInEasing), label = "splashTitleAlpha")
    val titleShift by animateFloatAsState(if (revealed) 0f else 18f, tween(900, delayMillis = 80, easing = FastOutSlowInEasing), label = "splashTitleShift")
    val tagAlpha by animateFloatAsState(if (revealed) 1f else 0f, tween(700, delayMillis = 240, easing = FastOutSlowInEasing), label = "splashTagAlpha")
    val tagShift by animateFloatAsState(if (revealed) 0f else 14f, tween(900, delayMillis = 240, easing = FastOutSlowInEasing), label = "splashTagShift")
    val glow = rememberInfiniteTransition(label = "splashGlow")
    val glowScale by glow.animateFloat(
        initialValue = 0.9f,
        targetValue = if (reducedMotion) 0.9f else 1.08f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "splashGlowScale"
    )

    var showSessionText by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(600)
        showSessionText = true
    }

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
                            .scale(glowScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Amber.copy(alpha = 0.17f), Amber.copy(alpha = 0.05f), Color.Transparent)
                                )
                            )
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_temacker_mark_on_dark),
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
                    modifier = Modifier.padding(top = 26.dp).graphicsLayer { alpha = titleAlpha; translationY = titleShift.dp.toPx() }
                )
                Text(
                    text = "TEAM COORDINATION",
                    color = Amber,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 12.dp).graphicsLayer { alpha = tagAlpha; translationY = tagShift.dp.toPx() }
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
                // Delayed 600ms per spec — avoids a flash of "Checking…" on a fast, already-cached session check.
                AnimatedVisibility(visible = showSessionText, enter = fadeIn()) {
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
}

@Preview
@Composable
private fun SplashScreenPreview() {
    TemackerTheme {
        SplashScreen()
    }
}
