package dev.ividi.weatherapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ividi.weatherapp.data.model.WeatherResponse
import dev.ividi.weatherapp.ui.common.CacheBadge
import dev.ividi.weatherapp.ui.common.weatherConditionGradient
import dev.ividi.weatherapp.ui.common.weatherConditionNeedsLightText

/**
 * The current-weather card: city/country, big temperature, description, feels-like/humidity/
 * wind, and the cache badge. The card background varies by weather condition keyword.
 */
@Composable
fun CurrentWeatherCard(weather: WeatherResponse, modifier: Modifier = Modifier) {
    val textColor = if (weatherConditionNeedsLightText(weather.description)) Color.White else Color.Black

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
            text = weather.description.replaceFirstChar { it.uppercase() },
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
                label = "Sensação",
                value = "${weather.feelsLike.roundedString()}${weather.units.temperatureSymbol}",
                textColor = textColor,
            )
            WeatherDetail(label = "Humidade", value = "${weather.humidity}%", textColor = textColor)
            WeatherDetail(
                label = "Vento",
                value = "${weather.windSpeed.roundedString()} ${weather.units.windSpeedUnit}",
                textColor = textColor,
            )
        }
    }
}

@Composable
private fun WeatherDetail(label: String, value: String, textColor: Color) {
    Column {
        Text(text = value, color = textColor, fontWeight = FontWeight.SemiBold)
        Text(text = label, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}

private fun Double.roundedString(): String {
    val rounded = kotlin.math.round(this).toInt()
    return rounded.toString()
}
