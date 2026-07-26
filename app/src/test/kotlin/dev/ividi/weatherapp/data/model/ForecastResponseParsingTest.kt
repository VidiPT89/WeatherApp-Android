package dev.ividi.weatherapp.data.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * The backend's `hourly[].time` field has NO timezone offset (e.g. "2024-01-01T00:00:00"),
 * so it must decode as a local [LocalDateTime], never as an [kotlinx.datetime.Instant] --
 * this fixture-based test pins that behavior against a literal JSON payload.
 */
class ForecastResponseParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val fixture = """
        {
          "city": "Lisboa",
          "country": "Portugal",
          "units": "metric",
          "provider": "open-meteo",
          "fromCache": false,
          "hourly": [
            { "time": "2024-01-01T00:00:00", "temperature": 12.0, "description": "clear sky", "precipitationProbability": 10 },
            { "time": "2024-01-01T01:00:00", "temperature": 11.5, "description": "clear sky", "precipitationProbability": 15 }
          ],
          "daily": [
            { "date": "2024-01-01", "temperatureMax": 18.0, "temperatureMin": 9.0, "description": "clear sky",
              "sunrise": "2024-01-01T07:45:00", "sunset": "2024-01-01T17:30:00", "uvIndexMax": 3.5, "precipitationProbabilityMax": 20,
              "windSpeedMax": 12.0, "rainLikely": false, "uvRiskLabel": "Moderate", "outdoorActivityLabel": "Good" },
            { "date": "2024-01-02", "temperatureMax": 19.5, "temperatureMin": 10.2, "description": "few clouds",
              "sunrise": "2024-01-02T07:45:00", "sunset": "2024-01-02T17:31:00", "uvIndexMax": 3.8, "precipitationProbabilityMax": 30,
              "windSpeedMax": 14.0, "rainLikely": false, "uvRiskLabel": "Moderate", "outdoorActivityLabel": "Good" }
          ]
        }
    """.trimIndent()

    @Test
    fun `decodes hourly time as a local date-time with no zone offset`() {
        val forecast = json.decodeFromString(ForecastResponse.serializer(), fixture)

        assertEquals(2, forecast.hourly.size)
        assertEquals(
            LocalDateTime(year = 2024, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0, second = 0),
            forecast.hourly[0].time,
        )
        assertEquals(
            LocalDateTime(year = 2024, monthNumber = 1, dayOfMonth = 1, hour = 1, minute = 0, second = 0),
            forecast.hourly[1].time,
        )
    }

    @Test
    fun `decodes daily date as a plain LocalDate`() {
        val forecast = json.decodeFromString(ForecastResponse.serializer(), fixture)

        assertEquals(2, forecast.daily.size)
        assertEquals(LocalDate(2024, 1, 1), forecast.daily[0].date)
        assertEquals(18.0, forecast.daily[0].temperatureMax, 0.0)
        assertEquals(9.0, forecast.daily[0].temperatureMin, 0.0)
    }

    @Test
    fun `provider and fromCache decode correctly for a forecast that is Open-Meteo-only`() {
        val forecast = json.decodeFromString(ForecastResponse.serializer(), fixture)

        assertEquals("open-meteo", forecast.provider)
        assertFalse(forecast.fromCache)
    }
}
