package dev.ividi.weatherapp.util

import dev.ividi.weatherapp.data.model.ProviderResult
import dev.ividi.weatherapp.data.model.Units
import dev.ividi.weatherapp.data.model.WeatherResponse
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CompareAverageTest {

    private val observedAt = Instant.parse("2024-06-01T12:00:00Z")

    private fun weatherWith(temperature: Double) = WeatherResponse(
        city = "Lisboa",
        country = "Portugal",
        temperature = temperature,
        feelsLike = temperature,
        humidity = 50,
        windSpeed = 10.0,
        description = "clear sky",
        units = Units.METRIC,
        provider = "open-meteo",
        observedAt = observedAt,
        fromCache = false,
    )

    @Test
    fun `returns null when fewer than two providers succeeded`() {
        val results = listOf(
            ProviderResult("open-meteo", success = true, weather = weatherWith(20.0), errorMessage = null),
            ProviderResult("open-weather-map", success = false, weather = null, errorMessage = "down"),
        )
        assertNull(averageTemperatureAcrossProviders(results))
    }

    @Test
    fun `averages temperature across every successful provider`() {
        val results = listOf(
            ProviderResult("open-meteo", success = true, weather = weatherWith(20.0), errorMessage = null),
            ProviderResult("open-weather-map", success = true, weather = weatherWith(24.0), errorMessage = null),
        )
        assertEquals(22.0, averageTemperatureAcrossProviders(results)!!, 0.0001)
    }

    @Test
    fun `ignores failed providers even if they somehow carry a weather payload`() {
        val results = listOf(
            ProviderResult("open-meteo", success = true, weather = weatherWith(10.0), errorMessage = null),
            ProviderResult("open-weather-map", success = true, weather = weatherWith(30.0), errorMessage = null),
            ProviderResult("third-provider", success = false, weather = weatherWith(100.0), errorMessage = "down"),
        )
        assertEquals(20.0, averageTemperatureAcrossProviders(results)!!, 0.0001)
    }

    @Test
    fun `returns null for an empty result list`() {
        assertNull(averageTemperatureAcrossProviders(emptyList()))
    }
}
