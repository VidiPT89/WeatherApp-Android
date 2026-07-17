package dev.ividi.weatherapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.ui.theme.FallbackAmber
import dev.ividi.weatherapp.ui.theme.FallbackAmberContainer

/**
 * Shown only when the backend served this weather from its secondary provider (i.e. the
 * primary, open-meteo, was unavailable) -- the fallback event this app exists to surface.
 */
@Composable
fun FallbackBanner(provider: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(FallbackAmberContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.WarningAmber,
            contentDescription = null,
            tint = FallbackAmber,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Provider principal indisponível — a usar alternativa ($provider)",
            color = FallbackAmber,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
