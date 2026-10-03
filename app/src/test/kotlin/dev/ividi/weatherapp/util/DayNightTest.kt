package dev.ividi.weatherapp.util

import dev.ividi.weatherapp.data.model.DailyForecastEntry
import dev.ividi.weatherapp.data.model.ForecastResponse
import dev.ividi.weatherapp.data.model.Units
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DayNightTest {

    private fun tokyoForecast(utcOffsetSeconds: Int?) = ForecastResponse(
        city = "Tokyo",
        country = "Japan",
        units = Units.METRIC,
        provider = "open-meteo",
        fromCache = false,
        hourly = emptyList(),
        daily = listOf(
            DailyForecastEntry(
                date = LocalDate.parse("2026-10-03"),
                temperatureMax = 24.0,
                temperatureMin = 17.0,
                description = "Clear sky",
                sunrise = LocalDateTime.parse("2026-10-03T05:35"),
                sunset = LocalDateTime.parse("2026-10-03T17:25"),
                uvIndexMax = 5.0,
                precipitationProbabilityMax = 0,
                windSpeedMax = 10.0,
                rainLikely = false,
                uvRiskLabel = "Moderate",
                outdoorActivityLabel = "Good",
            ),
        ),
        utcOffsetSeconds = utcOffsetSeconds,
    )

    @Test
    fun `noon in the city is day even when it is still night in UTC`() {
        // 12:00 in Tokyo (UTC+9) is 03:00 UTC, before the local sunrise if compared in UTC.
        val observedAt = Instant.parse("2026-10-03T03:00:00Z")
        assertFalse(tokyoForecast(utcOffsetSeconds = 9 * 3600).isNightAt(observedAt))
    }

    @Test
    fun `evening in the city is night even when it is daytime in UTC`() {
        // 21:00 in Tokyo is 12:00 UTC, which would look like midday if compared in UTC.
        val observedAt = Instant.parse("2026-10-03T12:00:00Z")
        assertTrue(tokyoForecast(utcOffsetSeconds = 9 * 3600).isNightAt(observedAt))
    }

    @Test
    fun `without daily entries it is never night`() {
        val forecast = tokyoForecast(utcOffsetSeconds = 9 * 3600).copy(daily = emptyList())
        assertFalse(forecast.isNightAt(Instant.parse("2026-10-03T12:00:00Z")))
    }
}
