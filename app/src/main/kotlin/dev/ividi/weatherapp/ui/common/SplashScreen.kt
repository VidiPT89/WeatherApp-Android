package dev.ividi.weatherapp.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val LOGO_ENTRANCE_DURATION_MS = 550
private const val TEXT_ENTRANCE_DELAY_MS = 250L
private const val TEXT_ENTRANCE_DURATION_MS = 450
private const val LOGO_SIZE_DP = 108
private const val GLOW_SIZE_DP = 150

/**
 * Brief branded splash shown at cold launch, with a minimum on-screen duration set by the
 * caller (`WeatherAppNavGraph`) so it never just flashes by. Mirrors iOS's `SplashView`: the
 * actual app icon (the adaptive-icon layers, so it stays a single source of truth with the
 * launcher icon) with a pulsing glow, a spring-like entrance, and a continuous animated wave
 * background echoing the icon's own sea motif -- reuses [weatherConditionGradient] instead of a
 * second set of brand colors.
 */
@Composable
fun SplashScreen() {
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        contentVisible = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0.6f,
        animationSpec = tween(LOGO_ENTRANCE_DURATION_MS),
        label = "logoScale",
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(LOGO_ENTRANCE_DURATION_MS),
        label = "logoAlpha",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(TEXT_ENTRANCE_DURATION_MS, delayMillis = TEXT_ENTRANCE_DELAY_MS.toInt()),
        label = "textAlpha",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "wavePhase",
    )

    Box(modifier = Modifier.fillMaxSize().background(weatherConditionGradient("clear sky"))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawWave(phase = wavePhase, amplitudeDp = 10f, wavelengthDp = 220f, heightFraction = 0.68f, alpha = 0.14f)
            drawWave(phase = wavePhase + 1.4f, amplitudeDp = 14f, wavelengthDp = 260f, heightFraction = 0.75f, alpha = 0.10f)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size((GLOW_SIZE_DP * pulse).dp)
                            .blur(radius = 20.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                    )

                    Box(
                        modifier = Modifier
                            .size((LOGO_SIZE_DP * logoScale).dp)
                            .alpha(logoAlpha)
                            .clip(RoundedCornerShape(24.dp)),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_background),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 16.dp).alpha(textAlpha)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(R.string.splash_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(textAlpha)) {
                Text(
                    text = stringResource(R.string.splash_creator_credit),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Text(
                    text = stringResource(R.string.splash_creator_website),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }
    }
}

/** A single horizontal sine wave, drawn as a filled band from [heightFraction] down to the bottom. */
private fun DrawScope.drawWave(phase: Float, amplitudeDp: Float, wavelengthDp: Float, heightFraction: Float, alpha: Float) {
    val amplitude = amplitudeDp.dp.toPx()
    val wavelength = wavelengthDp.dp.toPx()
    val baseY = size.height * heightFraction

    val path = Path().apply {
        moveTo(0f, baseY)
        var x = 0f
        val step = 8f
        while (x <= size.width) {
            val angle = (x / wavelength) * 2 * Math.PI.toFloat() + phase
            val y = baseY + sin(angle) * amplitude
            lineTo(x, y)
            x += step
        }
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    drawPath(path = path, color = Color.White.copy(alpha = alpha))
}
