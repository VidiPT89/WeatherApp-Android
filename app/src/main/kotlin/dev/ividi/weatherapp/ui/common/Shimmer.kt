package dev.ividi.weatherapp.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val SHIMMER_ANIMATION_DURATION_MS = 1_100
private const val SHIMMER_TRAVEL_DISTANCE = 1_000f

/**
 * A horizontally travelling gradient brush, the building block for shimmer-style skeleton
 * loaders shown while a screen's data is loading (replacing a plain spinner/"Loading…" text).
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = -SHIMMER_TRAVEL_DISTANCE,
        targetValue = SHIMMER_TRAVEL_DISTANCE,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_ANIMATION_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surface

    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnimation, 0f),
        end = Offset(translateAnimation + SHIMMER_TRAVEL_DISTANCE, SHIMMER_TRAVEL_DISTANCE),
    )
}

/** A single shimmering block, e.g. one line of skeleton text or one skeleton card. */
@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(rememberShimmerBrush(), shape),
    ) {}
}

/** A skeleton placeholder shaped roughly like [dev.ividi.weatherapp.ui.dashboard.CurrentWeatherCard]. */
@Composable
fun WeatherCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(rememberShimmerBrush(), RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        ShimmerBlock(height = 20.dp, modifier = Modifier.padding(bottom = 12.dp))
        ShimmerBlock(height = 48.dp, modifier = Modifier.padding(bottom = 12.dp))
        ShimmerBlock(height = 16.dp)
    }
}

/** A skeleton placeholder for the forecast chart area. */
@Composable
fun ChartSkeleton(modifier: Modifier = Modifier) {
    ShimmerBlock(modifier = modifier, height = 200.dp, shape = RoundedCornerShape(16.dp))
}
