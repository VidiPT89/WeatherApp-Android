package dev.ividi.weatherapp.data.model

import kotlinx.serialization.Serializable

/**
 * Mirrors the backend's `MarineResponse` JSON shape returned by `/api/v1/weather/marine`.
 *
 * All four data fields are nullable: for inland/non-coastal cities the backend still responds
 * with HTTP 200 but every field is `null`. That is a valid "no sea data for this city" result,
 * never an error -- callers must check [hasData] rather than treating nulls as a failure.
 *
 * Deliberately does not surface tide (high/low) times: the backend does not provide them.
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
) {
    val hasData: Boolean
        get() = waterTemperature != null ||
            waveHeightMeters != null ||
            waveDirectionDegrees != null ||
            wavePeriodSeconds != null
}
