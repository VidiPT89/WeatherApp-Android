package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.R
import dev.ividi.weatherapp.data.model.DailyForecastEntry
import dev.ividi.weatherapp.data.model.WeatherResponse
import dev.ividi.weatherapp.ui.common.CacheBadge
import dev.ividi.weatherapp.ui.common.weatherConditionGradient
import dev.ividi.weatherapp.ui.common.weatherConditionNeedsLightText
import dev.ividi.weatherapp.util.localizedWeatherDescription
import dev.ividi.weatherapp.util.toDisplayTime

/**
 * The current-weather card: city/country, big temperature, description, feels-like/humidity/
 * wind, sunrise/sunset/UV/rain-chance (sourced from [todayForecast], i.e. `daily[0]` -- the
 * current-weather endpoint itself doesn't carry them), and the cache badge. The card background
 * varies by weather condition keyword.
 */
@Composable
fun CurrentWeatherCard(
    weather: WeatherResponse,
    modifier: Modifier = Modifier,
    todayForecast: DailyForecastEntry? = null,
) {
    val textColor = if (weatherConditionNeedsLightText(weather.description)) Color.White else Color.Black
    val dividerColor = textColor.copy(alpha = 0.25f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(weatherConditionGradient(weather.description), RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = weather.city,
                    style = MaterialTheme.typography.titleLarge,
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
            text = localizedWeatherDescription(weather.description),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            WeatherDetail(
                label = stringResource(R.string.weather_feels_like),
                value = "${weather.feelsLike.roundedString()}${weather.units.temperatureSymbol}",
                textColor = textColor,
            )
            WeatherDetail(
                label = stringResource(R.string.weather_humidity),
                value = "${weather.humidity}%",
                textColor = textColor,
            )
            WeatherDetail(
                label = stringResource(R.string.weather_wind),
                value = "${weather.windSpeed.roundedString()} ${weather.units.windSpeedUnit}",
                textColor = textColor,
            )
        }

        if (todayForecast != null) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = dividerColor,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                WeatherDetail(
                    label = stringResource(R.string.weather_sunrise),
                    value = todayForecast.sunrise.toDisplayTime(),
                    textColor = textColor,
                    icon = Icons.Filled.WbTwilight,
                )
                WeatherDetail(
                    label = stringResource(R.string.weather_sunset),
                    value = todayForecast.sunset.toDisplayTime(),
                    textColor = textColor,
                    icon = Icons.Filled.WbSunny,
                )
                WeatherDetail(
                    label = stringResource(R.string.weather_uv_index),
                    value = todayForecast.uvIndexMax.roundedString(),
                    textColor = textColor,
                )
                WeatherDetail(
                    label = stringResource(R.string.weather_precipitation),
                    value = "${todayForecast.precipitationProbabilityMax}%",
                    textColor = textColor,
                    icon = Icons.Filled.WaterDrop,
                )
            }
        }
    }
}

@Composable
private fun WeatherDetail(
    label: String,
    value: String,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Column {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
        Text(text = value, color = textColor, fontWeight = FontWeight.SemiBold)
        Text(text = label, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}

private fun Double.roundedString(): String {
    val rounded = kotlin.math.round(this).toInt()
    return rounded.toString()
}
