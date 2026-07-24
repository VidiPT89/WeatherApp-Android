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
import dev.ividi.weatherapp.data.model.TideEvent
import dev.ividi.weatherapp.data.model.Units
import kotlin.math.roundToInt

/**
 * "Sea conditions" card: water temperature, swell (wave height/direction/period) and estimated
 * tide times from the `/marine` endpoint. When every sea-state field is null (inland/non-coastal
 * city), this renders a graceful "no data" message instead of treating the response as an error.
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

            if (marine.tideEvents.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = stringResource(R.string.marine_tides),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        marine.tideEvents.forEach { event ->
                            Text(
                                text = "${tideLabel(event)} ${formatTideTime(event.time)}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun tideLabel(event: TideEvent): String =
    stringResource(if (event.isHigh) R.string.marine_tide_high else R.string.marine_tide_low)

private fun formatTideTime(time: String): String = time.substringAfter("T", time)

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
