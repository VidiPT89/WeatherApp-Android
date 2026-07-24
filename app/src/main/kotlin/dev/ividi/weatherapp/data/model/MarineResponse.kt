package dev.ividi.weatherapp.data.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * A single high/low tide event, derived server-side from Open-Meteo's hourly
 * sea-level-height series -- an estimate, not an official tide table.
 */
@Serializable
data class TideEvent(
    val type: String,
    val time: LocalDateTime,
) {
    val isHigh: Boolean get() = type == "high"
}

/**
 * Mirrors the backend's `MarineResponse` JSON shape returned by `/api/v1/weather/marine`.
 *
 * The four sea-state fields are nullable: for inland/non-coastal cities the backend still
 * responds with HTTP 200 but every field is `null`. That is a valid "no sea data for this city"
 * result, never an error -- callers must check [hasData] rather than treating nulls as a failure.
 * [tideEvents] is empty for the same inland/non-coastal case.
 */
@Serializable
data class MarineResponse(
    val city: String,
    val country: String,
    val units: Units,
    val provider: String,
    val fromCache: Boolean,
    val waterTemperature: Double? = null,
    val waveHeightMeters: Double? = null,
    val waveDirectionDegrees: Double? = null,
    val wavePeriodSeconds: Double? = null,
    val tideEvents: List<TideEvent> = emptyList(),
) {
    val hasData: Boolean
        get() = waterTemperature != null ||
            waveHeightMeters != null ||
            waveDirectionDegrees != null ||
            wavePeriodSeconds != null
}
