package dev.ividi.weatherapp.data.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Mirrors the backend's `ForecastResponse` JSON shape returned by `/api/v1/weather/forecast`.
 *
 * NOTE: [HourlyForecastEntry.time] has no timezone offset in the wire payload (e.g.
 * "2024-01-01T00:00:00"), so it is parsed as a local [LocalDateTime], never as an [Instant].
 */
@Serializable
data class ForecastResponse(
    val city: String,
    val country: String,
    val units: Units,
    val provider: String,
    val fromCache: Boolean,
    val hourly: List<HourlyForecastEntry>,
    val daily: List<DailyForecastEntry>,
)

@Serializable
data class HourlyForecastEntry(
    val time: LocalDateTime,
    val temperature: Double,
    val description: String,
)

@Serializable
data class DailyForecastEntry(
    val date: LocalDate,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val description: String,
)
