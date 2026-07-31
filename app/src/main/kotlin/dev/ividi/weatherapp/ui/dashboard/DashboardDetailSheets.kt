package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.DailyForecastEntry
import dev.ividi.weatherapp.data.model.MarineResponse
import dev.ividi.weatherapp.data.model.TideEvent
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.model.WeatherResponse
import dev.ividi.weatherapp.ui.common.CacheBadge
import dev.ividi.weatherapp.ui.common.ConditionLabels
import dev.ividi.weatherapp.ui.common.weatherConditionGradient
import dev.ividi.weatherapp.ui.common.weatherConditionNeedsLightText
import dev.ividi.weatherapp.util.toDisplayTime
import kotlin.math.roundToInt

private const val INSIGHTS_DAYS_AHEAD = 7
private const val METERS_TO_FEET = 3.281

/**
 * Expanded view of the current-weather card, opened from a tap on [CurrentWeatherCard]. Repeats
 * everything already visible there (spaced out rather than crammed into three columns) plus the
 * [DailyForecastEntry] fields the compact card never shows at all: today's max wind, the rain-
 * likely call, and the UV-risk/outdoor-activity labels also surfaced (today-only) on
 * [WeatherInsightsCard].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherDetailSheet(weather: WeatherResponse, todayForecast: DailyForecastEntry?, modifier: Modifier = Modifier) {
    val textColor = if (weatherConditionNeedsLightText(weather.description)) Color.White else Color.Black

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(weatherConditionGradient(weather.description), RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    text = weather.city,
                    style = MaterialTheme.typography.headlineSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = weather.country, color = textColor)
            }
            CacheBadge(fromCache = weather.fromCache, observedAt = weather.observedAt)
        }

        Text(
            text = "${weather.temperature.roundedString()}${weather.units.temperatureSymbol}",
            style = MaterialTheme.typography.displayLarge,
            color = textColor,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = weather.description.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DetailStat(stringResource(R.string.weather_feels_like), "${weather.feelsLike.roundedString()}${weather.units.temperatureSymbol}", textColor)
            DetailStat(stringResource(R.string.weather_humidity), "${weather.humidity}%", textColor)
            DetailStat(stringResource(R.string.weather_wind), "${weather.windSpeed.roundedString()} ${weather.units.windSpeedUnit}", textColor)
            if (todayForecast != null) {
                DetailStat(
                    stringResource(R.string.weather_sunrise),
                    todayForecast.sunrise.toDisplayTime(),
                    textColor,
                    icon = Icons.Filled.WbTwilight,
                )
                DetailStat(
                    stringResource(R.string.weather_sunset),
                    todayForecast.sunset.toDisplayTime(),
                    textColor,
                    icon = Icons.Filled.WbSunny,
                )
                DetailStat(stringResource(R.string.weather_uv_index), todayForecast.uvIndexMax.roundedString(), textColor)
                DetailStat(
                    stringResource(R.string.weather_precipitation),
                    "${todayForecast.precipitationProbabilityMax}%",
                    textColor,
                    icon = Icons.Filled.WaterDrop,
                )
                DetailStat(
                    stringResource(R.string.weather_wind_max),
                    "${todayForecast.windSpeedMax.roundedString()} ${weather.units.windSpeedUnit}",
                    textColor,
                )
            }
        }

        if (todayForecast != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = textColor.copy(alpha = 0.25f))
            Text(
                text = stringResource(R.string.weather_detail_today_section),
                style = MaterialTheme.typography.titleSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DetailStat(
                    stringResource(R.string.weather_precipitation),
                    if (todayForecast.rainLikely) {
                        stringResource(R.string.weather_rain_likely_yes)
                    } else {
                        stringResource(R.string.weather_rain_likely_no)
                    },
                    textColor,
                )
                DetailStat(stringResource(R.string.insights_uv_risk), ConditionLabels.uvRisk(todayForecast.uvRiskLabel), textColor)
                DetailStat(
                    stringResource(R.string.insights_outdoor_activity),
                    ConditionLabels.outdoorActivity(todayForecast.outdoorActivityLabel),
                    textColor,
                )
            }
        }
    }
}

@Composable
private fun DetailStat(label: String, value: String, textColor: Color, icon: ImageVector? = null) {
    Column {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.padding(bottom = 2.dp))
        }
        Text(text = value, color = textColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        Text(text = label, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Expanded view of [SeaConditionsCard], opened from a tap on it: the same water-temp/wave-height/
 * direction/period stats with more breathing room, the full tide list (already shown in full on
 * the compact card, just restyled here), and today's [DailyForecastEntry] swell/fishing/surf
 * fields that the compact marine card never surfaces at all.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MarineDetailSheet(marine: MarineResponse, todayForecast: DailyForecastEntry?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(text = stringResource(R.string.marine_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = stringResource(R.string.marine_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )

        if (marine.hasData) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailStat(
                    stringResource(R.string.marine_water_temperature),
                    formatWaterTemperature(marine.waterTemperature, marine.units.temperatureSymbol),
                    MaterialTheme.colorScheme.onSurface,
                )
                DetailStat(
                    stringResource(R.string.marine_wave_height),
                    formatWaveHeight(marine.waveHeightMeters, marine.units == Units.IMPERIAL),
                    MaterialTheme.colorScheme.onSurface,
                )
                DetailStat(
                    stringResource(R.string.marine_wave_direction),
                    marine.waveDirectionDegrees?.let { "${it.roundToInt()}°" } ?: "—",
                    MaterialTheme.colorScheme.onSurface,
                )
                DetailStat(
                    stringResource(R.string.marine_wave_period),
                    marine.wavePeriodSeconds?.let { "${"%.1f".format(it)}s" } ?: "—",
                    MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.marine_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
        Text(text = stringResource(R.string.marine_tides), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (marine.tideEvents.isEmpty()) {
            Text(
                text = stringResource(R.string.marine_detail_tides_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                marine.tideEvents.forEach { event -> TideRow(event) }
            }
        }

        if (todayForecast?.waveHeightMax != null || todayForecast?.fishingConditionLabel != null || todayForecast?.surfConditionLabel != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))
            Text(
                text = stringResource(R.string.marine_detail_today_section),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                todayForecast.waveHeightMax?.let { waveMax ->
                    DetailStat(
                        stringResource(R.string.marine_wave_height),
                        formatWaveHeight(waveMax, marine.units == Units.IMPERIAL),
                        MaterialTheme.colorScheme.onSurface,
                    )
                }
                todayForecast.wavePeriodMax?.let { periodMax ->
                    DetailStat(stringResource(R.string.marine_wave_period), "${"%.1f".format(periodMax)}s", MaterialTheme.colorScheme.onSurface)
                }
                todayForecast.fishingConditionLabel?.let { label ->
                    DetailStat(stringResource(R.string.insights_fishing_conditions), ConditionLabels.fishingCondition(label), MaterialTheme.colorScheme.onSurface)
                }
                todayForecast.surfConditionLabel?.let { label ->
                    DetailStat(stringResource(R.string.insights_surf_conditions), ConditionLabels.surfCondition(label), MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun TideRow(event: TideEvent) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(if (event.isHigh) R.string.marine_tide_high else R.string.marine_tide_low),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(text = event.time.toDisplayTime(), style = MaterialTheme.typography.bodyLarge)
    }
}

private fun formatWaterTemperature(value: Double?, symbol: String): String {
    if (value == null) return "—"
    return "${value.roundToInt()}$symbol"
}

private fun formatWaveHeight(value: Double?, imperial: Boolean): String {
    if (value == null) return "—"
    return if (imperial) "${"%.1f".format(value * METERS_TO_FEET)} ft" else "${"%.1f".format(value)} m"
}

/**
 * Expanded view of [WeatherInsightsCard]: that card only ever shows *today's* moon phase/UV/
 * outdoor-activity/fishing indicators, even though the daily forecast already carries the same
 * per-day labels for the whole extended forecast. This reuses [DailyInsightList] (already built
 * for the Daily forecast tab) to show the next [INSIGHTS_DAYS_AHEAD] days at a glance instead of
 * repeating just today's entry.
 */
@Composable
fun InsightsDetailSheet(dailyEntries: List<DailyForecastEntry>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(text = stringResource(R.string.insights_multi_day_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = stringResource(R.string.insights_multi_day_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        DailyInsightList(entries = dailyEntries.take(INSIGHTS_DAYS_AHEAD))
    }
}

private fun Double.roundedString(): String = kotlin.math.round(this).toInt().toString()
