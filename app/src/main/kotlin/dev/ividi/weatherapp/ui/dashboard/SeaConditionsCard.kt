package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.MarineResponse
import dev.ividi.weatherapp.data.model.Units
import kotlin.math.roundToInt

/**
 * "Sea conditions" card: water temperature and swell (wave height/direction/period) from the
 * `/marine` endpoint. Deliberately never surfaces tide (high/low) times -- the backend does not
 * provide them. When every field is null (inland/non-coastal city), this renders a graceful
 * "no data" message instead of treating the response as an error.
 */
@Composable
fun SeaConditionsCard(marine: MarineResponse, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.marine_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.marine_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )

            if (marine.hasData) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SeaConditionDetail(
                        label = stringResource(R.string.marine_water_temperature),
                        value = formatWaterTemperature(marine.waterTemperature, marine.units),
                    )
                    SeaConditionDetail(
                        label = stringResource(R.string.marine_wave_height),
                        value = formatWaveHeight(marine.waveHeightMeters, marine.units),
                    )
                    SeaConditionDetail(
                        label = stringResource(R.string.marine_wave_direction),
                        value = marine.waveDirectionDegrees?.let { "${it.roundToInt()}°" } ?: "—",
                    )
                    SeaConditionDetail(
                        label = stringResource(R.string.marine_wave_period),
                        value = marine.wavePeriodSeconds?.let { "${"%.1f".format(it)}s" } ?: "—",
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.marine_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SeaConditionDetail(label: String, value: String) {
    Column {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

private fun formatWaterTemperature(value: Double?, units: Units): String {
    if (value == null) return "—"
    val symbol = if (units == Units.IMPERIAL) "°F" else "°C"
    return "${value.roundToInt()}$symbol"
}

private fun formatWaveHeight(value: Double?, units: Units): String {
    if (value == null) return "—"
    return if (units == Units.IMPERIAL) {
        "${"%.1f".format(value * 3.281)} ft"
    } else {
        "${"%.1f".format(value)} m"
    }
}
