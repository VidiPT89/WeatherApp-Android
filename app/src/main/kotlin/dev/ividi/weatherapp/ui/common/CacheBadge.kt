package dev.ividi.weatherapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.ui.theme.CacheAmberContainer
import dev.ividi.weatherapp.ui.theme.FreshGreen
import dev.ividi.weatherapp.ui.theme.FreshGreenContainer
import dev.ividi.weatherapp.util.cacheAgeBetween
import dev.ividi.weatherapp.util.toCompactDisplayString
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private const val TICK_INTERVAL_MS = 1_000L

/**
 * The cache badge: a green "fresh data" pill when [fromCache] is false, or an amber
 * "served from cache Xs/Xmin/Xh ago" pill -- ticking live once a second -- when true.
 */
@Composable
fun CacheBadge(fromCache: Boolean, observedAt: Instant, modifier: Modifier = Modifier) {
    if (!fromCache) {
        Pill(
            text = "Dados frescos",
            containerColor = FreshGreenContainer,
            contentColor = FreshGreen,
            modifier = modifier,
        )
        return
    }

    var now by remember(observedAt) { mutableStateOf(Clock.System.now()) }
    LaunchedEffect(observedAt) {
        while (true) {
            now = Clock.System.now()
            delay(TICK_INTERVAL_MS)
        }
    }

    val age = cacheAgeBetween(observedAt, now).toCompactDisplayString()
    Pill(
        text = "Servido da cache há $age",
        containerColor = CacheAmberContainer,
        contentColor = dev.ividi.weatherapp.ui.theme.CacheAmber,
        modifier = modifier,
    )
}

@Composable
private fun Pill(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = contentColor,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .background(containerColor, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
