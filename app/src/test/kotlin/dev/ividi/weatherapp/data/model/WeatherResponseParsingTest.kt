package dev.ividi.weatherapp.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherResponseParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes a fresh primary-provider weather response`() {
        val raw = """
            {
              "city": "Lisboa",
              "country": "Portugal",
              "temperature": 24.5,
              "feelsLike": 25.1,
              "humidity": 60,
              "windSpeed": 12.3,
              "description": "clear sky",
              "units": "metric",
              "provider": "open-meteo",
              "observedAt": "2024-06-01T12:00:00Z",
              "fromCache": false
            }
        """.trimIndent()

        val weather = json.decodeFromString(WeatherResponse.serializer(), raw)

        assertEquals("Lisboa", weather.city)
        assertEquals("Portugal", weather.country)
        assertEquals(24.5, weather.temperature, 0.0)
        assertEquals(Units.METRIC, weather.units)
        assertEquals("open-meteo", weather.provider)
        assertEquals(Instant.parse("2024-06-01T12:00:00Z"), weather.observedAt)
        assertFalse(weather.fromCache)
    }

    @Test
    fun `decodes a cached fallback-provider weather response with imperial units`() {
        val raw = """
            {
              "city": "Porto",
              "country": "Portugal",
              "temperature": 68.0,
              "feelsLike": 66.5,
              "humidity": 55,
              "windSpeed": 7.4,
              "description": "light rain",
              "units": "imperial",
              "provider": "open-weather-map",
              "observedAt": "2024-06-01T08:30:00Z",
              "fromCache": true
            }
        """.trimIndent()

        val weather = json.decodeFromString(WeatherResponse.serializer(), raw)

        assertEquals(Units.IMPERIAL, weather.units)
        assertEquals("open-weather-map", weather.provider)
        assertTrue(weather.fromCache)
    }
}
